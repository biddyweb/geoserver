/* Copyright (c) 2012 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs.xslt.config;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.impl.CatalogImpl;
import org.geoserver.catalog.impl.ResolvingProxy;
import org.geoserver.config.util.XStreamPersister;
import org.geoserver.ows.util.OwsUtils;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Transforms CatalogInfo into id references. Derived and heavily simplified from
 * {@link XStreamPersister}
 */
class ReferenceConverter implements Converter {
    Class clazz;

    private Catalog catalog;

    public ReferenceConverter(Class clazz, Catalog catalog) {
        this.catalog = catalog;
        this.clazz = clazz;
    }

    @SuppressWarnings("unchecked")
    public boolean canConvert(Class type) {
        return clazz.isAssignableFrom(type);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        // could be a proxy, unwrap it
        source = CatalogImpl.unwrap(source);

        // gets its id
        String id = (String) OwsUtils.get(source, "id");
        writer.startNode("id");
        writer.setValue(id);
        writer.endNode();
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        String ref = null;
        String pre = null;
        if (reader.hasMoreChildren()) {
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                ref = reader.getValue();
                reader.moveUp();
            }
        } else {
            ref = reader.getValue();
        }

        Object proxy = ResolvingProxy.create(ref, pre, clazz);
        Object resolved = proxy;
        if (catalog != null) {
            resolved = ResolvingProxy.resolve(catalog, proxy);
        }

        return resolved;

        // return CatalogImpl.unwrap(resolved);
    }
}