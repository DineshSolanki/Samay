package io.github.dineshsolanki.samay.strategy;

import jakarta.servlet.http.HttpServletRequest;

import java.time.ZoneId;

/**
 * Always resolves to a fixed, pre-configured timezone regardless of the request.
 */
public class FixedTimezoneResolver implements TimezoneResolver {

    private final ZoneId fixedZone;

    public FixedTimezoneResolver(ZoneId fixedZone) {
        this.fixedZone = fixedZone;
    }

    @Override
    public ZoneId resolve(HttpServletRequest request) {
        return fixedZone;
    }
}
