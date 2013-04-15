package ecologylab.tests.serialization.objectGraphTest;

import simpl.annotations.dbal.simpl_scalar;
import simpl.annotations.dbal.simpl_use_equals_equals;
import simpl.core.ElementState;

@simpl_use_equals_equals
public class PointEqEq extends ElementState {
	@simpl_scalar
	public int x;
	@simpl_scalar
	public int y;

	@Deprecated
	public PointEqEq() {}
	public PointEqEq(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public int hashCode() {
		return x+y;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof PointEqEq)) {
			return false;
		}
		PointEqEq o = (PointEqEq)other;
		return x == o.x && y == o.y;
	}
}