package behaviours;


/**
 * The class Records is created to encapsulate the data by using set and get methods.
 * @author Ming
 *
 */

public class Records {
	
	private long id;
	private double Pd;
	private double chp1;
	private double chp2;
	private double boiler;
	private double storage;

	
	public Records(long id, double Pd, double chp1, double chp2,
			double boiler, double storage) {
		super();
		this.id = id;
		this.Pd=Pd;
		this.chp1=chp1;
		this.chp2=chp2;
		this.boiler=boiler;
		this.storage=storage;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public double getPd() {
		return Pd;
	}
	
	public void setPd(double Pd) {
		this.Pd = Pd;
	}

	public double getChp1() {
		return chp1;
	}

	public void setChp1(double chp1) {
		this.chp1 = chp1;
	}

	public double getChp2() {
		return chp2;
	}

	public void setChp2(double chp2) {
		this.chp2 = chp2;
	}
	
	public double getBoiler() {
		return boiler;
	}

	public void setBoiler(double boiler) {
		this.boiler = boiler;
	}
	
	public double getStorage() {
		return storage;
	}

	public void setStorage(double storage) {
		this.storage=storage;
	}
	
	@Override
	public String toString() {
		return "Record [id=" + id + ", Pd=" + Pd
				+ ", chp1=" + chp1 + ", chp2=" + chp2 + ", boiler="
				+ boiler + ", storage="+ storage+"]";
	}
}
