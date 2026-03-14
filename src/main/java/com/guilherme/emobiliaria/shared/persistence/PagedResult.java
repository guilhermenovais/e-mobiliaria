package com.guilherme.emobiliaria.shared.persistence;

import java.util.List;

public record PagedResult<T>(List<T> items, long total) {}
