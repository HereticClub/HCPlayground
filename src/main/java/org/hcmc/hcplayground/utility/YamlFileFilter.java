package org.hcmc.hcplayground.utility;

import java.io.File;
import java.io.FilenameFilter;

public class YamlFileFilter implements FilenameFilter {

    public YamlFileFilter() {

    }

    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(".yml");
    }
}
