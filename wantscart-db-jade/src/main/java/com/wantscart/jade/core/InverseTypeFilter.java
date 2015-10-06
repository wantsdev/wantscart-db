package com.wantscart.jade.core;

import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;

class InverseTypeFilter implements TypeFilter {

    TypeFilter filter;

    public InverseTypeFilter(TypeFilter filter) {
        this.filter = filter;
    }

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
            throws IOException {
        return !filter.match(metadataReader, metadataReaderFactory);
    }
}
