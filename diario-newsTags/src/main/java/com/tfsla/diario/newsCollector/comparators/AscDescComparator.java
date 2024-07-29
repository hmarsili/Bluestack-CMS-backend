package com.tfsla.diario.newsCollector.comparators;

import java.util.Comparator;

public abstract class AscDescComparator<T> implements Comparator<T> {

	private int ascendentCoeficiet;
	
	public AscDescComparator(boolean asc) {
		super();
		this.ascendentCoeficiet = asc ? 1 : -1;
	}

	public int compare(T arg0, T arg1) {
		return this.naturalCompare(arg0, arg1) * this.getAscendentCoeficiet();
	}

	/**
	 * Hace la comparacion natural que despues invierte.
	 * @return
	 */
	protected abstract int naturalCompare(T obj1, T obj2);

	protected int getAscendentCoeficiet() {
		return this.ascendentCoeficiet;
	}
	
}
