
package fr.curie.vamp.gui;

public class Scale {

    private double alpha_x, alpha_y;
    private int beta_x, beta_y;
    private double y0;
    private double miny;

    public Scale(double alpha_x, double alpha_y) {
	this(alpha_x, 0, alpha_y, 0);
    }

    public Scale(double alpha_x, int beta_x, double alpha_y, int beta_y) {
	this(alpha_x, beta_x, alpha_y, beta_y, 0., 0.);
    }

    public Scale(double alpha_x, int beta_x, double alpha_y, int beta_y, double y0, double miny) {
	this.alpha_x = alpha_x;
	this.beta_x = beta_x;
	this.alpha_y = alpha_y;
	this.beta_y = beta_y;
	this.y0 = y0;
	this.miny = miny;
    }

    public double getX(long x) {
	return alpha_x * x + beta_x;
    }

    public double getWidth(double width) {
	return alpha_x * width;
    }

    public double getHeight(double height) {
	return alpha_y * height;
    }

    public double getY(double ratio) {
	//return beta_y - alpha_y * ratio;
	//return beta_y - alpha_y * (ratio - y0);
	return beta_y + alpha_y * (y0 - ratio);
    }

    public double getAlphaX() {
	return alpha_x;
    }

    public double getAlphaY() {
	return alpha_y;
    }

    public int getBetaX() {
	return beta_x;
    }

    public int getBetaY() {
	return beta_y;
    }

    public double getY0() {
	return y0;
    }

    public double getMinY() {
	return miny;
    }

    public void setAlphaX(double alpha_x) {
	this.alpha_x = alpha_x;
    }

    public void setAlphaY(double alpha_y) {
	this.alpha_y = alpha_y;
    }

    public void setBetaX(int beta_x) {
	this.beta_x = beta_x;
    }

    public void setBetaY(int beta_y) {
	this.beta_y = beta_y;
    }
}
