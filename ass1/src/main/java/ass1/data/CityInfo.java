package ass1.data;

public final class CityInfo {
	public final int geonameId;
	public final String name;
	public final String countryCode;
	public final String countryName;
	public final int population;
	public final String timezone;
	public final String coordinates;

	public CityInfo(int geonameId,      String name,    String countryCode,
					String countryName, int population, String timezone,
					String coordinates)
	{
		this.geonameId = geonameId;
		this.name = name;
		this.countryCode = countryCode;
		this.countryName = countryName;
		this.population = population;
		this.timezone = timezone;
		this.coordinates = coordinates;
	}

	@Override
	public String toString() {
		return String.format(
				"{ID: %d; Name: %s; Country code: %s; Country name: %s; Population: %d; Timezone: %s; Coordinates: %s}",
				this.geonameId,  this.name,     this.countryCode, this.countryName,
				this.population, this.timezone, this.coordinates
		);
	}

}
