package org.cytoscape.vsdl3c.internal.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * 
 * The SPARQL query template (parametric query) with its metadata in RDF, which
 * applys to CyNode
 * 
 */
public class SPARQLQueryTemplateForNode extends SPARQLQueryTemplate {

	public SPARQLQueryTemplateForNode(Resource query, CyNetwork network,
			CyNode node, PrefixMapping pm) {
		super(query, network, node, pm);
	}

	/**
	 * get the query string, by replacing the parameters with the corresponding
	 * column values of the CyNode
	 */
	public String getSPARQLQueryString() {
		String queryString = super.getSPARQLQueryString();
		for (Entry<String, String> entry : getParaName2NodeColumnNameMap()
				.entrySet()) {
			String v = entry.getValue();

			String value = this.pm.expandPrefix(network.getDefaultNodeTable()
					.getRow(id.getSUID())
					.get(this.pm.shortForm(entry.getValue()), String.class));
			queryString = queryString.replaceAll(entry.getKey(), value);
		}

		return queryString;
	}

	private Map<String, String> getParaName2NodeColumnNameMap() {
		HashMap<String, String> map = new HashMap<String, String>();

		StmtIterator it = query.listProperties(HAS_PARAMETER);
		while (it.hasNext()) {
			Statement stmt = it.nextStatement();
			RDFNode para = stmt.getObject();
			if (para.isResource()) {
				Resource r = (Resource) para;
				String paraName = r.getProperty(HAS_PARAMETER_NAME).getObject()
						.toString();
				String nodeColumnName = r.getProperty(HAS_COLUMN_NAME)
						.getObject().toString();
				if (paraName != null && nodeColumnName != null) {
					map.put(paraName, nodeColumnName);
				}

			}
		}
		return map;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cytoscape.vsdl3c.internal.model.SPARQLQueryTemplate#applysTo(org.
	 * cytoscape.model.CyIdentifiable)
	 */
	@Override
	public boolean applysTo(CyIdentifiable id) {
		Resource type = query.getPropertyResourceValue(APPLYS_TO);
		if (!NODE.equals(type)) {
			return false;
		}

		if (id instanceof CyNode) {
			CyNode node = (CyNode) id;

			// check if all of the contraints are satisfied
			StmtIterator it = query.listProperties(HAS_CONSTRAINT);
			while (it.hasNext()) {
				Statement stmt = it.nextStatement();
				RDFNode constraint = stmt.getObject();
				if (constraint.isResource()) {
					Resource r = (Resource) constraint;
					String column = r.getProperty(HAS_COLUMN_NAME).getObject()
							.toString();
					String value = r.getProperty(HAS_ATTRIBUTE_VALUE)
							.getObject().toString();
					if (column != null && value != null) {
						try {
							CyColumn cl = network.getDefaultNodeTable()
									.getColumn(this.pm.shortForm(column));
							if (cl == null) {
								return false;
							}
							Class<?> tp = cl.getType();

							if (tp == List.class) {
								List values = network
										.getDefaultNodeTable()
										.getRow(id.getSUID())
										.getList(this.pm.shortForm(column),
												cl.getListElementType());
								if (values == null) {
									return false;
								} else if (!values.contains(this.pm
										.shortForm(value))) {
									return false;
								}

							} else {
								String v = this.pm.expandPrefix(network
										.getDefaultNodeTable()
										.getRow(id.getSUID())
										.get(this.pm.shortForm(column),
												String.class));
								if (v == null) {
									return false;
								} else if (!v.contains(value)) {
									return false;
								}
							}

						} catch (Exception ex) {
							ex.printStackTrace();
							return false;
						}
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}
}
