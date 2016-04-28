package wff;

import java.util.List;

public interface UnifiableFormulaElement {
	public List<? extends UnifiableFormulaElement> getArguments();
}
