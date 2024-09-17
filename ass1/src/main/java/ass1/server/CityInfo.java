package ass1.server;

final class CityInfo {
	public final int geonameId;
	public final String name;
	public final String countryCode;
	public final String countryName;
	public final int population;
	public final String timezone;
	public final String coordinates;

	public CityInfo(int geonameId,
					String name,
					String countryCode,
					String countryName,
					int population,
					String timezone,
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
		return "\n===============================\n" +
				"ID: " + this.geonameId + "\n" +
				"Name: " + this.name + "\n" +
				"Country code: " + this.countryCode + "\n" +
				"Country name: " + this.countryName + "\n" +
				"Population: " + this.population + "\n" +
				"Timezone: " + this.timezone + "\n" +
				"Coordinates: " + this.coordinates +
				"\n===============================\n";
	}
}
