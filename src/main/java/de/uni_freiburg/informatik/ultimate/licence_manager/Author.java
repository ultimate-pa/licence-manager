package de.uni_freiburg.informatik.ultimate.licence_manager;

public class Author {

	public String Name;
	public String YearFrom;
	public String YearTo;

	public Author(String name, String yearFrom, String yearTo) {
		Name = name;
		YearFrom = yearFrom;
		YearTo = yearTo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Name == null) ? 0 : Name.hashCode());
		result = prime * result
				+ ((YearFrom == null) ? 0 : YearFrom.hashCode());
		result = prime * result + ((YearTo == null) ? 0 : YearTo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Author other = (Author) obj;
		if (Name == null) {
			if (other.Name != null)
				return false;
		} else if (!Name.equals(other.Name))
			return false;
		if (YearFrom == null) {
			if (other.YearFrom != null)
				return false;
		} else if (!YearFrom.equals(other.YearFrom))
			return false;
		if (YearTo == null) {
			if (other.YearTo != null)
				return false;
		} else if (!YearTo.equals(other.YearTo))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return YearFrom + "-" + YearTo + " " + Name;
	}
}
