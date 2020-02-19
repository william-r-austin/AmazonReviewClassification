package cs584.project1;

public enum Rating {
	POSITIVE("+1"),
	NEGATIVE("-1"),
	NONE("0");
	
	private String label;
	
	private Rating(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
}
