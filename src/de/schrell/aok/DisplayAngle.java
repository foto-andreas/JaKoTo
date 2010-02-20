package de.schrell.aok;

public class DisplayAngle {

	int normalz=0;
	Aok aok = null;
	
	public DisplayAngle(Aok aok) {
		this.aok = aok;
	}
	
	public void calc() {

		double Rad2Deg = 360.0 / Math.PI;
		double mx, my, mz, gx, gy, gz;
		
		if (normalz==0) normalz = aok.getAokState(49);
		
		mx = aok.getAokState(44)-aok.getAokConfig(46);
		my = aok.getAokState(45)-aok.getAokConfig(47);
		mz = aok.getAokState(46)-aok.getAokConfig(48);
		
		gx = aok.getAokState(47)-aok.getAokConfig(4);
		gy = aok.getAokState(48)-aok.getAokConfig(5);
		gz = aok.getAokState(49)-normalz;

		// I'm using the vector method of tilt compensation rather than a cosine
		// matrix. The vector method does not involve
		// a lot of trig. Basically what is going on is that horizontal plane is
		// constructed using Cross products of g and m.
		// Then by using Dot products, the body coordinate frame is projected
		// onto the newly contructed horizontal XY vectors.
		// for more information search:
		// "A New Solution Algorithm of Magnetic Amizuth", or
		// "A combined electronic compass/clinometer"
		double hx = (gz * gz * mx) - (gx * gz * mz) - (gx * gy * my)
				+ (gy * gy * mx); // horizontal X mag vector
		double hy = (gy * mz) - (gz * my); // horizontal Y mag vector
		double heading = Math.atan2(hy, hx) * Rad2Deg;

		if (heading > 0)
			heading = 360 - heading;
		else
			heading = -heading;
		
		System.out.printf("heading: %f\n", heading);

	}

}
