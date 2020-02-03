package org.checkerframework.dataflow.cfg;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.cfg.block.Block;
import org.checkerframework.dataflow.cfg.block.Block.BlockType;
import org.checkerframework.dataflow.cfg.block.ConditionalBlock;
import org.checkerframework.dataflow.cfg.block.ExceptionBlock;
import org.checkerframework.dataflow.cfg.block.SingleSuccessorBlock;
import org.checkerframework.dataflow.cfg.block.SpecialBlock;
import org.checkerframework.dataflow.cfg.block.SpecialBlockImpl;
import org.checkerframework.dataflow.cfg.node.AssignmentNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.ReturnNode;

/** A control flow graph (CFG for short) of a single method. */
public class ControlFlowGraph {

    /** The entry block of the control flow graph. */
    protected final SpecialBlock entryBlock;

    /** The regular exit block of the control flow graph. */
    protected final SpecialBlock regularExitBlock;

    /** The exceptional exit block of the control flow graph. */
    protected final SpecialBlock exceptionalExitBlock;

    /** The AST this CFG corresponds to. */
    protected final UnderlyingAST underlyingAST;

    /**
     * Maps from AST {@link Tree}s to sets of {@link Node}s. Every Tree that produces a value will
     * have at least one corresponding Node. Trees that undergo conversions, such as boxing or
     * unboxing, can map to two distinct Nodes. The Node for the pre-conversion value is stored in
     * treeLookup, while the Node for the post-conversion value is stored in convertedTreeLookup.
     */
    protected final IdentityHashMap<Tree, Set<Node>> treeLookup;

    /** Map from AST {@link Tree}s to post-conversion sets of {@link Node}s. */
    protected final IdentityHashMap<Tree, Set<Node>> convertedTreeLookup;

    /** Map from AST {@link UnaryTree}s to corresponding {@link AssignmentNode}s. */
    protected final IdentityHashMap<UnaryTree, AssignmentNode> unaryAssignNodeLookup;

    /**
     * All return nodes (if any) encountered. Only includes return statements that actually return
     * something
     */
    protected final List<ReturnNode> returnNodes;

    /**
     * Class declarations that have been encountered when building the control-flow graph for a
     * method.
     */
    protected final List<ClassTree> declaredClasses;

    /**
     * Lambdas encountered when building the control-flow graph for a method, variable initializer,
     * or initializer.
     */
    protected final List<LambdaExpressionTree> declaredLambdas;

    public ControlFlowGraph(
            SpecialBlock entryBlock,
            SpecialBlockImpl regularExitBlock,
            SpecialBlockImpl exceptionalExitBlock,
            UnderlyingAST underlyingAST,
            IdentityHashMap<Tree, Set<Node>> treeLookup,
            IdentityHashMap<Tree, Set<Node>> convertedTreeLookup,
            IdentityHashMap<UnaryTree, AssignmentNode> unaryAssignNodeLookup,
            List<ReturnNode> returnNodes,
            List<ClassTree> declaredClasses,
            List<LambdaExpressionTree> declaredLambdas) {
        super();
        this.entryBlock = entryBlock;
        this.underlyingAST = underlyingAST;
        this.treeLookup = treeLookup;
        this.unaryAssignNodeLookup = unaryAssignNodeLookup;
        this.convertedTreeLookup = convertedTreeLookup;
        this.regularExitBlock = regularExitBlock;
        this.exceptionalExitBlock = exceptionalExitBlock;
        this.returnNodes = returnNodes;
        this.declaredClasses = declaredClasses;
        this.declaredLambdas = declaredLambdas;
    }

    /**
     * @return the set of {@link Node}s to which the {@link Tree} {@code t} corresponds. Returns
     *     null for trees that don't produce a value.
     */
    public Set<Node> getNodesCorrespondingToTree(Tree t) {
        if (convertedTreeLookup.containsKey(t)) {
            return convertedTreeLookup.get(t);
        } else {
            return treeLookup.get(t);
        }
    }

    /** @return the entry block of the control flow graph. */
    public SpecialBlock getEntryBlock() {
        return entryBlock;
    }

    public List<ReturnNode> getReturnNodes() {
        return returnNodes;
    }

    public SpecialBlock getRegularExitBlock() {
        return regularExitBlock;
    }

    public SpecialBlock getExceptionalExitBlock() {
        return exceptionalExitBlock;
    }

    /** @return the AST this CFG corresponds to. */
    public UnderlyingAST getUnderlyingAST() {
        return underlyingAST;
    }

    /** @return the set of all basic block in this control flow graph */
    public Set<Block> getAllBlocks() {
        Set<Block> visited = new HashSet<>();
        Queue<Block> worklist = new ArrayDeque<>();
        Block cur = entryBlock;
        visited.add(entryBlock);

        // traverse the whole control flow graph
        while (true) {
            if (cur == null) {
                break;
            }

            Deque<Block> succs = getSuccessors(cur);

            for (Block b : succs) {
                if (!visited.contains(b)) {
                    visited.add(b);
                    worklist.add(b);
                }
            }

            cur = worklist.poll();
        }

        return visited;
    }

    /**
     * @return the list of all basic block in this control flow graph in reversed depth-first
     *     postorder sequence.
     *     <p>Blocks may appear more than once in the sequence.
     */
    public List<Block> getDepthFirstOrderedBlocks() {
        List<Block> dfsOrderResult = new ArrayList<>();
        Set<Block> visited = new HashSet<>();
        Deque<Block> worklist = new ArrayDeque<>();
        worklist.add(entryBlock);
        while (!worklist.isEmpty()) {
            Block cur = worklist.getLast();
            if (visited.contains(cur)) {
                dfsOrderResult.add(cur);
                worklist.removeLast();
            } else {
                visited.add(cur);
                Deque<Block> successors = getSuccessors(cur);
                successors.removeAll(visited);
                if (cur.getType() == BlockType.CONDITIONAL_BLOCK) {
                    // Ensure then block is processed before else block.
                    ConditionalBlock cb = (ConditionalBlock) cur;
                    worklist.add(cb.getElseSuccessor());
                    worklist.add(cb.getThenSuccessor());
                } else {
                    worklist.addAll(successors);
                }
            }
        }

        Collections.reverse(dfsOrderResult);
        return dfsOrderResult;
    }

    /**
     * Get a list of all successor Blocks for cur.
     *
     * @return a Deque of successor Blocks
     */
    private Deque<Block> getSuccessors(Block cur) {
        Deque<Block> succs = new ArrayDeque<>();
        if (cur.getType() == BlockType.CONDITIONAL_BLOCK) {
            ConditionalBlock ccur = ((ConditionalBlock) cur);
            succs.add(ccur.getThenSuccessor());
            succs.add(ccur.getElseSuccessor());
        } else {
            assert cur instanceof SingleSuccessorBlock;
            Block b = ((SingleSuccessorBlock) cur).getSuccessor();
            if (b != null) {
                succs.add(b);
            }
        }

        if (cur.getType() == BlockType.EXCEPTION_BLOCK) {
            ExceptionBlock ecur = (ExceptionBlock) cur;
            for (Set<Block> exceptionSuccSet : ecur.getExceptionalSuccessors().values()) {
                succs.addAll(exceptionSuccSet);
            }
        }
        return succs;
    }

    /** @return the copied tree-lookup map */
    public IdentityHashMap<Tree, Set<Node>> getTreeLookup() {
        return new IdentityHashMap<>(treeLookup);
    }

    /** @return the copied lookup-map of the assign node for unary operation */
    public IdentityHashMap<UnaryTree, AssignmentNode> getUnaryAssignNodeLookup() {
        return new IdentityHashMap<>(unaryAssignNodeLookup);
    }

    /**
     * Get the {@link MethodTree} of the CFG if the argument {@link Tree} maps to a {@link Node} in
     * the CFG or null otherwise.
     */
    public @Nullable MethodTree getContainingMethod(Tree t) {
        if (treeLookup.containsKey(t)) {
            if (underlyingAST.getKind() == UnderlyingAST.Kind.METHOD) {
                UnderlyingAST.CFGMethod cfgMethod = (UnderlyingAST.CFGMethod) underlyingAST;
                return cfgMethod.getMethod();
            }
        }
        return null;
    }

    /**
     * Get the {@link ClassTree} of the CFG if the argument {@link Tree} maps to a {@link Node} in
     * the CFG or null otherwise.
     */
    public @Nullable ClassTree getContainingClass(Tree t) {
        if (treeLookup.containsKey(t)) {
            if (underlyingAST.getKind() == UnderlyingAST.Kind.METHOD) {
                UnderlyingAST.CFGMethod cfgMethod = (UnderlyingAST.CFGMethod) underlyingAST;
                return cfgMethod.getClassTree();
            }
        }
        return null;
    }

    public List<ClassTree> getDeclaredClasses() {
        return declaredClasses;
    }

    public List<LambdaExpressionTree> getDeclaredLambdas() {
        return declaredLambdas;
    }
}
