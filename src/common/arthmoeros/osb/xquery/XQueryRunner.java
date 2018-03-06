package common.arthmoeros.osb.xquery;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQSequence;
import javax.xml.xquery.XQSequenceType;
import javax.xml.xquery.XQStaticContext;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Node;

import com.bea.wli.common.xquery.XQExecOptions;
import com.bea.wli.common.xquery.x10.XQueryExecutor;
import com.bea.wli.common.xquery.x10.XQueryExecutorFactory;

import oracle.xml.xquery.OXQDataSource;

public class XQueryRunner {

	/**
	 * Usage: command xqueryfile.xqy outputfile.xml param1=parameter1.xml
	 * param2=parameter2 paramN=parameterN
	 * 
	 * Parameters must have the xquery parameter name specified like
	 * "parameter=value".
	 * 
	 * The runner will determine internally the value type based on the xquery file
	 * declaration, if it is an "anyType" will use the parameter value as a xml file
	 * path to look for
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length == 0 || args[0].equals("")) {
			throw new IllegalArgumentException("XQuery file is not specified");
		} else if (args.length == 1 || args[1].equals("")) {
			throw new IllegalArgumentException("Output file is not specified");
		}

		OXQDataSource ds = new OXQDataSource();
		XQConnection con = ds.getConnection();

		XQStaticContext ctx = con.getStaticContext();

		XQueryExecutorFactory xqef = new XQueryExecutorFactory();
		String xqueryFile = readXQueryFile(args[0], Charset.defaultCharset());
		XQueryExecutor xqe = xqef.create(xqueryFile, con, ctx);

		if (xqe.getVariables().length > (args.length - 2)) {
			throw new IllegalArgumentException(
					"Number of parameters doesn't match with expected parameters for the specified XQuery");
		}

		int inputIndex = 2;
		Map<String, String> inputParams = new HashMap<>();
		for (int i = 2; i < args.length; i++) {
			String[] paramValue = args[i].split("=");
			if (paramValue.length != 2) {
				throw new IllegalArgumentException("Parameter " + args[i] + " is not in a valid format");
			}
			inputParams.put(paramValue[0], paramValue[1]);

		}
		Map<QName, Object> params = new HashMap<>();
		for (QName qn : xqe.getXQTypes().keySet()) {
			XQSequenceType type = xqe.getXQTypes().get(qn);
			if (type.getItemType().getBaseType() == XQItemType.XQBASETYPE_ANYTYPE
					|| type.getItemType().getBaseType() == XQItemType.XQBASETYPE_UNTYPED) {
				params.put(qn, readXmlFile(inputParams.get(qn.getLocalPart())));
			} else if (type.getItemType().getBaseType() == XQItemType.XQBASETYPE_STRING) {
				params.put(qn, inputParams.get(qn.getLocalPart()));
			} else if (type.getItemType().getBaseType() == XQItemType.XQBASETYPE_INT
					|| type.getItemType().getBaseType() == XQItemType.XQBASETYPE_INTEGER) {
				params.put(qn, Integer.valueOf(inputParams.get(qn.getLocalPart())));
			} else if (type.getItemType().getBaseType() == XQItemType.XQBASETYPE_LONG) {
				params.put(qn, Long.valueOf(inputParams.get(qn.getLocalPart())));
			} else if (type.getItemType().getBaseType() == XQItemType.XQBASETYPE_DOUBLE) {
				params.put(qn, Double.valueOf(inputParams.get(qn.getLocalPart())));
			} else {
				throw new IllegalArgumentException("Unrecognized type in prefix for: " + args[inputIndex]);
			}
			inputIndex++;
		}

		XQExecOptions xqeo = new XQExecOptions();
		XQSequence seq = xqe.execute(null, params, xqeo);

		seq.next();
		Files.write(new File(args[1]).toPath(), nodeToString(seq.getNode()).getBytes(), StandardOpenOption.CREATE);
	}

	private static XmlObject readXmlFile(String path) throws IOException, XmlException {
		return XmlObject.Factory.parse(new File(path));
	}

	private static String readXQueryFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return injectBeaFunctionsModule(encoded, encoding);
	}

	private static String injectBeaFunctionsModule(byte[] encoded, Charset encoding) {
		StringBuilder file = new StringBuilder(new String(encoded, encoding));

		Pattern p = Pattern.compile("xquery.*?\n");
		Matcher m = p.matcher(file.toString());
		int end = 0;
		if (m.find()) {
			end = m.end();
		}
		file.insert(end, "import module namespace fn-bea = 'http://www.bea.com/xquery/xquery-functions';\n");
		return file.toString();
	}

	private static String nodeToString(Node node) {
		try {
			// Create and setup transformer
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

			// Turn the node into a string
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(node), new StreamResult(writer));
			return writer.toString();
		} catch (TransformerException e) {
			throw new RuntimeException(e);
		}
	}

}
