/* 
 * The MIT License
 *
 * Copyright 2017 Michał Szymański, kontakt: michal.szymanski.aajar@gmail.com.
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
package crawler.scrapping.collectors;

import crawler.utils.ClassSet;
import java.util.Arrays;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 *
 * @author Michał Szymański, kontakt: michal.szymanski.aajar@gmail.com
 */
public class CollectorValidator implements ConstraintValidator<Valid, Collector> {

    @Override
    public void initialize(Valid a) {
    }

    @Override
    public boolean isValid(Collector t, ConstraintValidatorContext cvc) {

        if (t.accepts() == null && t.produces() == null) {
            return false;
        }

        ClassSet accepts = t.accepts();
        ClassSet produces = t.produces();

        return (isInstanceOfDomCollector(t) && hasValidProducesDeclaration(produces) || hasValidAcceptsDeclaration(accepts));
    }

    private boolean isInstanceOfDomCollector(Collector c) {

        return c instanceof DomCollector;
    }

    private boolean hasValidAcceptsDeclaration(ClassSet accepts) {

        return !(accepts.stream().anyMatch((el) -> Arrays.asList(Collector.SECURED_TYPES).contains(el)));
    }

    private boolean hasValidProducesDeclaration(ClassSet produces) {

        return !(produces.stream().anyMatch((el) -> Arrays.asList(Collector.SECURED_TYPES).contains(el)));
    }
}
