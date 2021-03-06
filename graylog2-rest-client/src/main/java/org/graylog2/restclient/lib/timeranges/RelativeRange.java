/**
 * The MIT License
 * Copyright (c) 2012 TORCH GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.graylog2.restclient.lib.timeranges;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class RelativeRange extends TimeRange {

    private final int range;

    public RelativeRange(int range) throws InvalidRangeParametersException {
        if (range < 0) {
            throw new InvalidRangeParametersException();
        }

        this.range = range;
    }

    public TimeRange.Type getType() {
        return Type.RELATIVE;
    }

    @Override
    public Map<String, String> getQueryParams() {
        return new HashMap<String, String>() {{
            put("range_type", getType().toString().toLowerCase());
            put("range", String.valueOf(range));
        }};
    }

    @Override
    public String toString() {
        StringBuilder sb =  new StringBuilder("Relative time range [").append(this.getClass().getCanonicalName()).append("] - ");
        sb.append("range: ").append(this.range);

        return sb.toString();
    }

    /* Indicates if the range value is 0 */
    public boolean isEmptyRange() {
        return range == 0;
    }

}
