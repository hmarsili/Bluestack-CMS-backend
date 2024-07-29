package com.tfsla.diario.model;

import java.util.Date;

import com.tfsla.diario.comentarios.model.Comment;

public class TfsComentario {

	Comment comment = null;
	
	public TfsComentario()
	{
		
	}
	
	public TfsComentario(Comment comment)
	{
		this.comment = comment;
	}
	
	public String getUsername()
	{
		return comment.getUser();
	}
	
	public int getId()
	{
		return comment.getId();
	}
	
	public String getState() {
		return comment.getState();
	}
	
	public Date getDate()
	{
		return comment.getDate();
	}
	
	public int getReports()
	{
		return comment.getCantReports();
	}
	
	public String getText()
	{
		return comment.getText();
	}
	
	public int getCommentCount()
	{
		return comment.getCommentCount();
	}
	
	public void setCommentCount(int size)
	{
		comment.setCommentCount(size);
	}
	
	public int getParentId() {
		return comment.getReplyoOf();
	}
	
}
