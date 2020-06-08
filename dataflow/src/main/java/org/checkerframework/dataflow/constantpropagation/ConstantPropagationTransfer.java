package org.checkerframework.dataflow.constantpropagation;

import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.ConditionalTransferResult;
import org.checkerframework.dataflow.analysis.RegularTransferResult;
import org.checkerframework.dataflow.analysis.StoreSet;
import org.checkerframework.dataflow.analysis.TransferFunction;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.UnderlyingAST;
import org.checkerframework.dataflow.cfg.node.AbstractNodeVisitor;
import org.checkerframework.dataflow.cfg.node.AssignmentNode;
import org.checkerframework.dataflow.cfg.node.EqualToNode;
import org.checkerframework.dataflow.cfg.node.IntegerLiteralNode;
import org.checkerframework.dataflow.cfg.node.LocalVariableNode;
import org.checkerframework.dataflow.cfg.node.Node;

public class ConstantPropagationTransfer
        extends AbstractNodeVisitor<
                TransferResult<Constant, ConstantPropagationStore>,
                TransferInput<Constant, ConstantPropagationStore>>
        implements TransferFunction<Constant, ConstantPropagationStore> {

    @Override
    public StoreSet<ConstantPropagationStore> initialStore(
            UnderlyingAST underlyingAST, @Nullable List<LocalVariableNode> parameters) {
        ConstantPropagationStore store = new ConstantPropagationStore();
        return new StoreSet<>(store);
    }

    @Override
    public TransferResult<Constant, ConstantPropagationStore> visitLocalVariable(
            LocalVariableNode node, TransferInput<Constant, ConstantPropagationStore> before) {
        StoreSet<ConstantPropagationStore> store = before.getRegularStore();
        Constant value = null;
        for (ConstantPropagationStore s : store.getStores()) {
            if (value == null) {
                value = s.getInformation(node);
            }
            value = value.leastUpperBound(s.getInformation(node));
        }
        return new RegularTransferResult<>(value, store);
    }

    @Override
    public TransferResult<Constant, ConstantPropagationStore> visitNode(
            Node n, TransferInput<Constant, ConstantPropagationStore> p) {
        return new RegularTransferResult<>(null, p.getRegularStore());
    }

    @Override
    public TransferResult<Constant, ConstantPropagationStore> visitAssignment(
            AssignmentNode n, TransferInput<Constant, ConstantPropagationStore> pi) {
        StoreSet<ConstantPropagationStore> p = pi.getRegularStore();
        Node target = n.getTarget();
        Constant info = null;
        if (target instanceof LocalVariableNode) {
            LocalVariableNode t = (LocalVariableNode) target;
            for (ConstantPropagationStore s : p.getStores()) {
                info = s.getInformation(n.getExpression());
                s.setInformation(t, info);
            }
        }
        return new RegularTransferResult<>(info, p);
    }

    @Override
    public TransferResult<Constant, ConstantPropagationStore> visitIntegerLiteral(
            IntegerLiteralNode n, TransferInput<Constant, ConstantPropagationStore> pi) {
        StoreSet<ConstantPropagationStore> p = pi.getRegularStore();
        Constant val = null;
        for (ConstantPropagationStore s : p.getStores()) {
            Constant c = new Constant(n.getValue());
            s.setInformation(n, c);
            if (val == null) {
                val = c;
            }
            val = val.leastUpperBound(c);
        }
        return new RegularTransferResult<>(val, p);
    }

    @Override
    public TransferResult<Constant, ConstantPropagationStore> visitEqualTo(
            EqualToNode n, TransferInput<Constant, ConstantPropagationStore> pi) {
        StoreSet<ConstantPropagationStore> p = pi.getRegularStore();
        StoreSet<ConstantPropagationStore> old = p.copy();
        Node left = n.getLeftOperand();
        Node right = n.getRightOperand();
        process(p, left, right);
        process(p, right, left);
        return new ConditionalTransferResult<>(null, p, old);
    }

    protected void process(StoreSet<ConstantPropagationStore> p, Node a, Node b) {
        for (ConstantPropagationStore s : p.getStores()) {
            Constant val = s.getInformation(a);
            if (b instanceof LocalVariableNode && val.isConstant()) {
                s.setInformation(b, val);
            }
        }
    }
}
