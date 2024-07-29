package org.opencms.search.indexExcludeCondition;
import java.util.HashMap;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

public class EvalIdxConditionVisitor  extends idxConditionBaseVisitor<Object> {
	 private final Map<String, Object> variables;

	 private CmsObject cms=null;
	 private CmsResource resource=null;
	 
	  public EvalIdxConditionVisitor(CmsObject cms, CmsResource resource) {
	    this.variables = new HashMap<String, Object>();
	    this.cms = cms;
	    this.resource = resource;
	  }

	  @Override
	  public Object visitParse(idxConditionParser.ParseContext ctx) {
	    return super.visit(ctx.expression());
	  }

	  @Override
	  public Object visitStringExpression(idxConditionParser.StringExpressionContext ctx) {
	    return ctx.STRING().getText().replaceAll("\"", "");
	  }
	  
	  
	  @Override
	  public Object visitDecimalExpression(idxConditionParser.DecimalExpressionContext ctx) {
	    return Double.valueOf(ctx.DECIMAL().getText());
	  }

	  @Override
	  public Object visitIdentifierExpression(idxConditionParser.IdentifierExpressionContext ctx) {
		  Object result = variables.get(ctx.IDENTIFIER().getText());
		  if (result!=null)
			  return result;
		  
		  CmsProperty prop;
		try {
				prop = cms.readPropertyObject(resource, ctx.IDENTIFIER().getText(), true);
				String value = "";
				if (prop!=null && prop.getValue()!=null)
					value = prop.getValue();
				
				variables.put(ctx.IDENTIFIER().getText(),value);
			  
				return variables.get(ctx.IDENTIFIER().getText());
		} catch (CmsException e) {
			e.printStackTrace();
		}
		return "";
	    
	  }

	  @Override
	  public Object visitNotExpression(idxConditionParser.NotExpressionContext ctx) {
	    return !((Boolean)this.visit(ctx.expression()));
	  }

	  @Override
	  public Object visitParenExpression(idxConditionParser.ParenExpressionContext ctx) {
	    return super.visit(ctx.expression());
	  }
	  
	  @Override
	  public Object visitComparatorExpression(idxConditionParser.ComparatorExpressionContext ctx) {
	    if (ctx.op.EQ() != null) {
	      return this.visit(ctx.left).equals(this.visit(ctx.right));
	    }
	    else if (ctx.op.NEQ() != null) {
	    	return !this.visit(ctx.left).equals(this.visit(ctx.right));
		}
	    else if (ctx.op.LE() != null) {
	      return asDouble(ctx.left) <= asDouble(ctx.right);
	    }
	    else if (ctx.op.GE() != null) {
	      return asDouble(ctx.left) >= asDouble(ctx.right);
	    }
	    else if (ctx.op.LT() != null) {
	      return asDouble(ctx.left) < asDouble(ctx.right);
	    }
	    else if (ctx.op.GT() != null) {
	      return asDouble(ctx.left) > asDouble(ctx.right);
	    }
	    throw new RuntimeException("not implemented: comparator operator " + ctx.op.getText());
	  }

	  @Override
	  public Object visitBinaryExpression(idxConditionParser.BinaryExpressionContext ctx) {
	    if (ctx.op.AND() != null) {
	      return asBoolean(ctx.left) && asBoolean(ctx.right);
	    }
	    else if (ctx.op.OR() != null) {
	      return asBoolean(ctx.left) || asBoolean(ctx.right);
	    }
	    throw new RuntimeException("not implemented: binary operator " + ctx.op.getText());
	  }

	  @Override
	  public Object visitBoolExpression(idxConditionParser.BoolExpressionContext ctx) {
	    return Boolean.valueOf(ctx.getText());
	  }

	  private boolean asBoolean(idxConditionParser.ExpressionContext ctx) {
		  Object res = visit(ctx) ;
		    if (res.getClass() == String.class)
			  return Boolean.parseBoolean((String)visit(ctx));
		    else 
		    	return (boolean)res;
	  }

	  private double asDouble(idxConditionParser.ExpressionContext ctx) {
	    Object res = visit(ctx) ;
	    if (res.getClass() == String.class)
		  return Double.parseDouble((String)visit(ctx));
	    else 
	    	return (double)res;
	  }

}
