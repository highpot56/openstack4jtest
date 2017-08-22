package test.sample;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.api.storage.ObjectStorageService;
import org.openstack4j.core.transport.Config;
import org.openstack4j.core.transport.ProxyHost;
import org.openstack4j.model.common.DLPayload;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.storage.object.SwiftObject;
import org.openstack4j.openstack.OSFactory;

/**
 * Servlet implementation class Test01
 */
@WebServlet("/Test01")
public class Test01 extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String USERNAME = "c5c7e1a1ab884b1496fac1b8cdeeba67";
	private static final String PASSWORD = "xRR}w#~AT1,U43UX";
	private static final String DOMAIN_ID = "1413043";
	private static final String PROJECT_ID = "5412455628a04c53950ab788deec35aa";

	private ObjectStorageService authenticateAndGetObjectStorageService() {
		String OBJECT_STORAGE_AUTH_URL = "https://identity.open.softlayer.com/v3";

		Identifier domainIdentifier = Identifier.byName(DOMAIN_ID);

		System.out.println("Authenticating...");

//		System.setProperty("http.proxyHost","http://mtc-px14");
//		System.setProperty("http.proxyPort", "8081");

		ProxyHost proxy = ProxyHost.of("http://mtc-px14", 8081);
		Config config = Config.newConfig().withProxy(proxy);

		OSClientV3 os = OSFactory.builderV3()
				.endpoint(OBJECT_STORAGE_AUTH_URL)
				.withConfig(config)
				//.credentials(USERNAME,PASSWORD, domainIdentifier)
				.credentials(USERNAME, PASSWORD)
				.scopeToProject(Identifier.byId(PROJECT_ID))
				.authenticate();

		System.out.println("Authenticated successfully!");

		ObjectStorageService objectStorage = os.objectStorage();

		return objectStorage;
	}

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Test01() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ObjectStorageService objectStorage = authenticateAndGetObjectStorageService();

		System.out.println("Retrieving file from ObjectStorage...");

		String containerName = request.getParameter("container");

		String fileName = request.getParameter("file");

		if(containerName == null || fileName == null){ //No file was specified to be found, or container name is missing
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			System.out.println("Container name or file name was not specified.");
			return;
		}

		SwiftObject pictureObj = objectStorage.objects().get(containerName,fileName);

		if(pictureObj == null){ //The specified file was not found
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			System.out.println("File not found.");
			return;
		}

		String mimeType = pictureObj.getMimeType();

		DLPayload payload = pictureObj.download();

		InputStream in = payload.getInputStream();

		response.setContentType(mimeType);

		OutputStream out = response.getOutputStream();

		IOUtils.copy(in, out);
		in.close();
		out.close();

		System.out.println("Successfully retrieved file from ObjectStorage!");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
