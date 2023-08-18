package diminishingcoloursbot;

import java.math.BigDecimal;

public class ColorConverters {

    public static float[] RGBToOkLab(float[] values) {
        return RGBToOkLab(new Values(values));
    }
    public static float[] RGBToOkLab(Values c) {
        var l = BigDecimal.valueOf(0.4122214708f).multiply(BigDecimal.valueOf(c.r)).add(BigDecimal.valueOf(0.5363325363f).multiply(BigDecimal.valueOf(c.g))).add(BigDecimal.valueOf(0.0514459929f).multiply(BigDecimal.valueOf(c.b)));
        var m = BigDecimal.valueOf(0.2119034982f).multiply(BigDecimal.valueOf(c.r)).add(BigDecimal.valueOf(0.6806995451f).multiply(BigDecimal.valueOf(c.g))).add(BigDecimal.valueOf(0.1073969566f).multiply(BigDecimal.valueOf(c.b)));
        var s = BigDecimal.valueOf(0.0883024619f).multiply(BigDecimal.valueOf(c.r)).add(BigDecimal.valueOf(0.2817188376f).multiply(BigDecimal.valueOf(c.g))).add(BigDecimal.valueOf(0.6299787005f).multiply(BigDecimal.valueOf(c.b)));
    
        var l_ = l.pow(1/3);
        var m_ = m.pow(1/3);
        var s_ = s.pow(1/3);

        return new float[] {
            l_.multiply(BigDecimal.valueOf(0.2104542553f)).add(m_.multiply(BigDecimal.valueOf(0.7936177850f))).add(s_.multiply(BigDecimal.valueOf(-0.0040720468f))).floatValue(),
            l_.multiply(BigDecimal.valueOf(1.9779984951f)).add(m_.multiply(BigDecimal.valueOf(-2.4285922050f))).add(s_.multiply(BigDecimal.valueOf(0.4505937099f))).floatValue(),
            l_.multiply(BigDecimal.valueOf(0.0259040371f)).add(m_.multiply(BigDecimal.valueOf(0.7827717662f))).add(s_.multiply(BigDecimal.valueOf(-0.8086757660f))).floatValue(),
        };
    }

    public static float[] OkLabToRGB(float[] values) {
        return OkLabToRGB(new Values(values));
    }
    public static float[] OkLabToRGB(Values c) {

        var l_ = BigDecimal.valueOf(c.r).add(BigDecimal.valueOf(0.3963377774f).multiply(BigDecimal.valueOf(c.g))).add(BigDecimal.valueOf(0.2158037573f).multiply(BigDecimal.valueOf(c.b)));
        var m_ = BigDecimal.valueOf(c.r).add(BigDecimal.valueOf(-0.1055613458f).multiply(BigDecimal.valueOf(c.g))).add(BigDecimal.valueOf(-0.0638541728f).multiply(BigDecimal.valueOf(c.b)));
        var s_ = BigDecimal.valueOf(c.r).add(BigDecimal.valueOf(-0.0894841775f).multiply(BigDecimal.valueOf(c.g))).add(BigDecimal.valueOf(-1.2914855480f).multiply(BigDecimal.valueOf(c.b)));
    
        var l = l_.pow(3);
        var m = m_.pow(3);
        var s = s_.pow(3);

        return new float[] {
            l.multiply(BigDecimal.valueOf(4.0767416621f)).add(m.multiply(BigDecimal.valueOf(-3.3077115913f))).add(s.multiply(BigDecimal.valueOf(0.2309699292f))).floatValue(),
            l.multiply(BigDecimal.valueOf(-1.2684380046f)).add(m.multiply(BigDecimal.valueOf(2.6097574011f))).add(s.multiply(BigDecimal.valueOf(-0.3413193965f))).floatValue(),
            l.multiply(BigDecimal.valueOf(-0.0041960863f)).add(m.multiply(BigDecimal.valueOf(-0.7034186147f))).add(s.multiply(BigDecimal.valueOf(1.7076147010f))).floatValue(),
        };
    }
    
    private static class Values {
        private float r;
        private float g;
        private float b;
        
        private Values(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        private Values(float[] values) {
            this(values[0], values[1], values[2]);
        }
    }
}
