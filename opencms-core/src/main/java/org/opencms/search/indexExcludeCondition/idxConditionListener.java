package org.opencms.search.indexExcludeCondition;

// Generated from idxCondition.g4 by ANTLR 4.7.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link idxConditionParser}.
 */
public interface idxConditionListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link idxConditionParser#parse}.
	 * @param ctx the parse tree
	 */
	void enterParse(idxConditionParser.ParseContext ctx);
	/**
	 * Exit a parse tree produced by {@link idxConditionParser#parse}.
	 * @param ctx the parse tree
	 */
	void exitParse(idxConditionParser.ParseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryExpression}
	 * labeled alternative in {@link idxConditionParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryExpression(idxConditionParser.BinaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryExpression}
	 * labeled alternative in {@link idxConditionParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryExpression(idxConditionParser.BinaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code decimalExpression}
	 * labeled alternative in {@link idxConditionParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterDecimalExpression(idxConditionParser.DecimalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code decimalExpression}
	 * labeled alternative in {@link idxConditionParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitDecimalExpression(idxConditionParser.DecimalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringExpression}
	 * labeled alternative in {@link idxConditionParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterStringExpression(idxConditionParser.StringExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringExpression}
	 * labeled alternative in {@link idxConditionParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitStringExpression(idxConditionParser.StringExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code boolExpression}
	 * labeled alternative in {@link idxConditionParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBoolExpression(idxConditionParser.BoolExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code boolExpression}
	 * labeled alternative in {@link idxConditionParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBoolExpression(idxConditionParser.BoolExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code identifierExpression}
	 * labeled alternative in {@link idxConditionParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierExpression(idxConditionParser.IdentifierExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code identifierExpression}
	 * labeled alternative in {@link idxConditionParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierExpression(idxConditionParser.IdentifierExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link idxConditionParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNotExpression(idxConditionParser.NotExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link idxConditionParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNotExpression(idxConditionParser.NotExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parenExpression}
	 * labeled alternative in {@link idxConditionParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterParenExpression(idxConditionParser.ParenExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parenExpression}
	 * labeled alternative in {@link idxConditionParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitParenExpression(idxConditionParser.ParenExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code comparatorExpression}
	 * labeled alternative in {@link idxConditionParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterComparatorExpression(idxConditionParser.ComparatorExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code comparatorExpression}
	 * labeled alternative in {@link idxConditionParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitComparatorExpression(idxConditionParser.ComparatorExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link idxConditionParser#comparator}.
	 * @param ctx the parse tree
	 */
	void enterComparator(idxConditionParser.ComparatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link idxConditionParser#comparator}.
	 * @param ctx the parse tree
	 */
	void exitComparator(idxConditionParser.ComparatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link idxConditionParser#binary}.
	 * @param ctx the parse tree
	 */
	void enterBinary(idxConditionParser.BinaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link idxConditionParser#binary}.
	 * @param ctx the parse tree
	 */
	void exitBinary(idxConditionParser.BinaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link idxConditionParser#bool}.
	 * @param ctx the parse tree
	 */
	void enterBool(idxConditionParser.BoolContext ctx);
	/**
	 * Exit a parse tree produced by {@link idxConditionParser#bool}.
	 * @param ctx the parse tree
	 */
	void exitBool(idxConditionParser.BoolContext ctx);
}