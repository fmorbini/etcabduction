package wff;

public class NumericConstant extends Constant {
	private Float value=null;
	public NumericConstant(Float n) {
		super(null);
		this.value=n;
	}

	public Float getValue() {
		return value;
	}

	@Override
	public String getName() {
		return getValue()+"";
	}
}
