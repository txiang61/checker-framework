package org.checkerframework.dataflow.analysis;

import org.checkerframework.dataflow.cfg.node.ConditionalAndNode;
import org.checkerframework.dataflow.cfg.node.ConditionalNotNode;
import org.checkerframework.dataflow.cfg.node.ConditionalOrNode;
import org.checkerframework.dataflow.cfg.node.EqualToNode;
import org.checkerframework.dataflow.cfg.node.GreaterThanNode;
import org.checkerframework.dataflow.cfg.node.GreaterThanOrEqualNode;
import org.checkerframework.dataflow.cfg.node.LessThanNode;
import org.checkerframework.dataflow.cfg.node.LessThanOrEqualNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.NotEqualNode;

/**
 * Evaluates the flow at a boolean expression. This evaluator is used for dead branch elimination.
 * If return is THEN, then the else branch is a dead branch. If return is ELSE, then the then branch
 * is a dead branch. If return is BOTH, then both branches are not dead.
 */
public class ConditionEvaluator<V extends AbstractValue<V>, S extends Store<S>> {

    public enum ConditionalFlow {
        THEN,
        ELSE,
        BOTH
    }

    /**
     * Evaluate a boolean expression and returns the flow at the condition.
     *
     * @param node the boolean node to be evaluated on
     * @param in the transfer input storing the values of the node
     * @return the flow at a boolean expression
     */
    private ConditionalFlow visitNode(Node node, TransferInput<V, S> in) {
        if (node instanceof GreaterThanNode) {
            return visitGreaterThan((GreaterThanNode) node, in);
        }
        if (node instanceof GreaterThanOrEqualNode) {
            return visitGreaterThanOrEqual((GreaterThanOrEqualNode) node, in);
        }
        if (node instanceof LessThanOrEqualNode) {
            return visitLessThanOrEqual((LessThanOrEqualNode) node, in);
        }
        if (node instanceof LessThanNode) {
            return visitLessThan((LessThanNode) node, in);
        }
        if (node instanceof EqualToNode) {
            return visitEqualTo((EqualToNode) node, in);
        }
        if (node instanceof NotEqualNode) {
            return visitNotEqual((NotEqualNode) node, in);
        }
        if (node instanceof ConditionalAndNode) {
            return visitConditionalAnd((ConditionalAndNode) node, in);
        }
        if (node instanceof ConditionalOrNode) {
            return visitConditionalOr((ConditionalOrNode) node, in);
        }
        if (node instanceof ConditionalNotNode) {
            return visitConditionalNot((ConditionalNotNode) node, in);
        }
        return ConditionalFlow.BOTH;
    }

    /**
     * Evaluate a greater than node and returns the flow at the condition.
     *
     * @param node the greater than node to be evaluated on
     * @param in the transfer input storing the values of the node
     * @return the flow at a boolean expression
     */
    public ConditionalFlow visitGreaterThan(GreaterThanNode node, TransferInput<V, S> in) {
        return ConditionalFlow.BOTH;
    }

    /**
     * Evaluate a greater than or equal node and returns the flow at the condition.
     *
     * @param node the greater than or equal node to be evaluated on
     * @param in the transfer input storing the values of the node
     * @return the flow at a boolean expression
     */
    public ConditionalFlow visitGreaterThanOrEqual(
            GreaterThanOrEqualNode node, TransferInput<V, S> in) {
        return ConditionalFlow.BOTH;
    }

    /**
     * Evaluate a less than or equal node and returns the flow at the condition.
     *
     * @param node the less than or equal node to be evaluated on
     * @param in the transfer input storing the values of the node
     * @return the flow at a boolean expression
     */
    public ConditionalFlow visitLessThanOrEqual(LessThanOrEqualNode node, TransferInput<V, S> in) {
        return ConditionalFlow.BOTH;
    }

    /**
     * Evaluate a less than node and returns the flow at the condition.
     *
     * @param node the less than node to be evaluated on
     * @param in the transfer input storing the values of the node
     * @return the flow at a boolean expression
     */
    public ConditionalFlow visitLessThan(LessThanNode node, TransferInput<V, S> in) {
        return ConditionalFlow.BOTH;
    }

    /**
     * Evaluate a equal to node and returns the flow at the condition.
     *
     * @param node the equal to node to be evaluated on
     * @param in the transfer input storing the values of the node
     * @return the flow at a boolean expression
     */
    public ConditionalFlow visitEqualTo(EqualToNode node, TransferInput<V, S> in) {
        return ConditionalFlow.BOTH;
    }

    /**
     * Evaluate a not equal node and returns the flow at the condition.
     *
     * @param node the not equal node to be evaluated on
     * @param in the transfer input storing the values of the node
     * @return the flow at a boolean expression
     */
    public ConditionalFlow visitNotEqual(NotEqualNode node, TransferInput<V, S> in) {
        return ConditionalFlow.BOTH;
    }

    /**
     * Evaluate a conditional AND node and returns the flow at the condition. Both right and left
     * expression are individually evaluated to determine the flow of branch.
     *
     * @param node the conditional and node to be evaluated on
     * @param in the transfer input storing the values of the node
     * @return the flow at a boolean expression
     */
    public ConditionalFlow visitConditionalAnd(ConditionalAndNode node, TransferInput<V, S> in) {
        ConditionalFlow left = visitNode(node.getLeftOperand(), in);
        ConditionalFlow right = visitNode(node.getRightOperand(), in);
        if (left == ConditionalFlow.THEN && right == ConditionalFlow.THEN) {
            return ConditionalFlow.THEN;
        } else if (left == ConditionalFlow.ELSE || right == ConditionalFlow.ELSE) {
            return ConditionalFlow.ELSE;
        }
        return ConditionalFlow.BOTH;
    }

    /**
     * Evaluate a conditional OR node and returns the flow at the condition. Both right and left
     * expression are individually evaluated to determine the flow of branch.
     *
     * @param node the conditional and node to be evaluated on
     * @param in the transfer input storing the values of the node
     * @return the flow at a boolean expression
     */
    public ConditionalFlow visitConditionalOr(ConditionalOrNode node, TransferInput<V, S> in) {
        ConditionalFlow left = visitNode(node.getLeftOperand(), in);
        ConditionalFlow right = visitNode(node.getRightOperand(), in);
        if (left == ConditionalFlow.THEN || right == ConditionalFlow.THEN) {
            return ConditionalFlow.THEN;
        } else if (left == ConditionalFlow.ELSE && right == ConditionalFlow.ELSE) {
            return ConditionalFlow.ELSE;
        }
        return ConditionalFlow.BOTH;
    }

    /**
     * Evaluate a conditional NOT node and returns the flow at the condition.
     *
     * @param node the conditional and node to be evaluated on
     * @param in the transfer input storing the values of the node
     * @return the flow at a boolean expression
     */
    public ConditionalFlow visitConditionalNot(ConditionalNotNode node, TransferInput<V, S> in) {
        ConditionalFlow sub = visitNode(node.getOperand(), in);
        if (sub == ConditionalFlow.THEN) {
            return ConditionalFlow.ELSE;
        }
        if (sub == ConditionalFlow.ELSE) {
            return ConditionalFlow.THEN;
        }
        return ConditionalFlow.BOTH;
    }
}
