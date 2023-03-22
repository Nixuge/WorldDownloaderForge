package wdl.gui.notifications.shapes.base;

public abstract class ShapeRounded extends Shape {
    public ShapeRounded(int radius) {
        super();
        this.radius = radius;
    }

    public ShapeRounded(int color, int radius) {
        super(color);
        this.radius = radius;
    }

    protected static int ROUNDING_STEP = 1;

    protected int radius;
    protected double[][] positions;

    /**
     * @param radius radius of the circle
     * 
     * Get the proper step to use when drawing the circle.
     * NOTE: THIS DOESN'T NEED TO BE USED. AN INT OF 1 LOOKS BETTER.
     * 
     * @return the step to use.
     */
    protected float getProperStep(float radius) {
        int n = 0;
        while (true) {
            float f1 = 2 * (float)Math.PI * radius / n;
            float f2 = 2 * radius * (float)Math.sin(Math.PI / n);
            float f3 = Math.abs(f1 - f2) / f1;
            if (f3 < 0.0003) {
                return 360 / (float) n;
            }
            n++;
        }
    }

    protected void calculateRoundPositions(int x, int y, int startingDegree, int endingDegree) {
        int length = endingDegree - startingDegree + 1;
        this.positions = new double[length][];
        
        for (int i = startingDegree; i <= endingDegree ; i += ROUNDING_STEP) {
            double angleRad = i * Math.PI / 180.0;
            double xHere = x + radius * Math.cos(angleRad);
            double yHere = y + radius * Math.sin(angleRad);

            this.positions[i - startingDegree] = new double[] {xHere, yHere};
        }
    }
}
