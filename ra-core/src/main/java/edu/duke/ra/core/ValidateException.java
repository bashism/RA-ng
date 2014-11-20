package edu.duke.ra.core;

import java.sql.SQLException;

public class ValidateException extends Exception {
	private static final long serialVersionUID = -6001535968345951159L;
	protected SQLException _sqlException;
	protected RAXNode _errorNode;
	public ValidateException(SQLException sqlException, RAXNode errorNode) {
		_sqlException = sqlException;
		_errorNode = errorNode;
	}
	public ValidateException(String message, RAXNode errorNode) {
		super(message);
		_sqlException = null;
		_errorNode = errorNode;
	}
	public SQLException getSQLException() {
		return _sqlException;
	}
	public RAXNode getErrorNode() {
		return _errorNode;
	}
}
