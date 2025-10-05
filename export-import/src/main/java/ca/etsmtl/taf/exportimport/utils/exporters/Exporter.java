package ca.etsmtl.taf.exportimport.utils.exporters;

import java.util.List;
import java.util.Map;

public interface Exporter {

    public void exportTo(Map<String, List<String>> ids) throws Exception;

}
