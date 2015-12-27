package test.org.fun4j;

import java.math.BigDecimal;

public class Box {
    
    private Double p1x;
    private Double p1y;
    private Double p2x;
    private Double p2y;
    
    public Double height;
    
    public static final int CONSTANT = 42;
    
    public Box() {
        p1x = 0.0;
        p1y = 0.0;
        p2x = 0.0;
        p2y = 0.0;
        height = Math.abs(p2y - p1y);
    }
    
    public Box(Double x1, Double y1, Double x2, Double y2) {
        p1x = x1;
        p1y = y1;
        p2x = x2;
        p2y = y2;
        height = Math.abs(p2y - p1y);
    }
    
    public static int getConstant() {
        return CONSTANT;
    }
    
    public Double getHeight() {
        return height;
    }
    
    public Double getP1x() {
        return p1x;
    }
    public void setP1x(Double p1x) {
        this.p1x = p1x;
    }
    public Double getP1y() {
        return p1y;
    }
    public void setP1y(Double p1y) {
        this.p1y = p1y;
    }
    public Double getP2x() {
        return p2x;
    }
    public void setP2x(Double p2x) {
        this.p2x = p2x;
    }
    public Double getP2y() {
        return p2y;
    }
    public void setP2y(Double p2y) {
        this.p2y = p2y;
    }
    
    public Double getSize() {
        return Math.abs(p2x - p1x) * Math.abs(p2y - p1y);
    }
    
    public Boolean contains(Double px, Double py) {
        return p1x <= px && px <= p2x && p1y <= py && py <= p2y; 
    }
    
    public Boolean contains(BigDecimal px, BigDecimal py) {
        return p1x <= px.doubleValue() 
        && px.doubleValue() <= p2x 
        && p1y <= py.doubleValue() 
        && py.doubleValue() <= p2y; 
    }
    
    
}