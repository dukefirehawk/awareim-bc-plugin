package com.neural;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.neural.json.Customer;
import com.neural.json.Option;
import com.neural.json.OptionSet;
import com.neural.json.OptionValue;
import com.neural.json.Product;
import com.neural.json.Sku;
import com.neural.json.parser.IBigCommerceParser;
import com.neural.json.parser.OptionJsonParser;
import com.neural.json.parser.OptionSetJsonParser;
import com.neural.json.parser.OptionValueJsonParser;
import com.neural.json.parser.ProductJsonParser;
import com.neural.json.parser.SkuJsonParser;

public class BigCommerceConnector {

	private static Log log = LogFactory.getLog(BigCommerceConnector.class);

	private String storeUrl; // "https://store-r5adzn.mybigcommerce.com/api/v2";
	private String username; // "thomas_hii";
	private String apikey;   // "ddee9404ad8c2f5698dae9270879fd7d3abb78ac";
	
	private String basicAuth;
    
    private InputStream responseStream;
    private HttpURLConnection transport;
    
    private String responseData;

    public BigCommerceConnector(String storeUrl, String username, String apikey)
    {
        this.storeUrl = storeUrl;
        this.username = username;
        this.apikey = apikey;
        
        // Create the basic auth
        String token = this.username + ":" + this.apikey;
		this.basicAuth = "Basic " + DatatypeConverter.printBase64Binary(token.getBytes());
		
		//System.out.println("Auth: " + this.basicAuth);
    }

    /**
     * Get encoded string representing HTTP Basic authorization credentials for the request.
     */
    private String getBasicAuthHeader() {

        return basicAuth;
    }

    /**
     * Create the HTTP connection transport to specified URL path.
     */
    private HttpURLConnection createTransport(String path, String verb) throws java.io.IOException {
        URL url = new URL(this.storeUrl + path);
        HttpURLConnection transport = (HttpURLConnection) url.openConnection();
        transport.setRequestMethod(verb);
        transport.setRequestProperty("Authorization", this.getBasicAuthHeader());
        return transport;
    }

    /**
     * Make an HTTP GET request to the given endpoint.
     */
    private BigCommerceConnector get(String path)
    {
		if (transport != null) {
            transport.disconnect();
        }

		try {

			transport = this.createTransport(path, "GET");
    		this.responseStream = transport.getInputStream();

		} catch(Exception e) {
			error(" ", e);
		}

		return this;
    }
    
    /**
     * Close any existing HTTP connection.
     */
    private void closeExistingConnection() {
		if (transport != null) {
            transport.disconnect();
            transport = null;
        }
    }
    
    /**
     * Close any open connection.
     */
    private void close() {
    	closeExistingConnection();
    }
    
    
    

    /**
     * Make an HTTP POST request to the given endpoint.
     */
    private boolean post(String path, String data)
    {
    	
    	closeExistingConnection();

        try {
            transport = this.createTransport(path, "POST");
	        transport.setDoOutput(true);
	        
            OutputStreamWriter post = new OutputStreamWriter(transport.getOutputStream());
            post.write(data);
            post.flush();
            post.close();

            this.responseStream = transport.getInputStream();

        } catch(Exception e) {
			error(" ", e);
        }

        return false;
    }

    /**
     * Make an HTTP PUT request to the given endpoint.
     */
    private boolean put(String path, String data)
    {
    	closeExistingConnection();

        try {
            transport = this.createTransport(path, "PUT");
	        this.responseStream = transport.getInputStream();
	        return true;
        } catch(Exception e) {
			error(" ", e);
        }

        return false;
    }

    /**
     * Make an HTTP DELETE request to the given endpoint.
     */
    private boolean delete(String path)
    {
    	closeExistingConnection();

        try {
            transport = this.createTransport(path, "DELETE");
            return true;
        } catch(Exception e) {
			error(" ", e);
        }

        return false;
    }

    /**
     * String representation of the raw HTTP response body.
     */
    private String asString()
    {
    	String responseBody = "";

    	try {
            StringBuffer body = new StringBuffer();
    		BufferedReader reader = new BufferedReader(new InputStreamReader(this.responseStream, "UTF-8"));
    		String inputLine;
    		while ((inputLine = reader.readLine()) != null) {
    			body.append(inputLine);
    		}
    		reader.close();
    		responseBody = body.toString();

    	} catch (Exception e) {
			error(" ", e);
    	} finally {
    		closeExistingConnection();
    	}

		return responseBody;
    }

    /**
     * XML representation of the HTTP response.
     */
     private Element asXml()
     {
    	 Element responseXml = null;

    	 try {
   	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
   	        DocumentBuilder builder = factory.newDocumentBuilder();
   	        Document document = builder.parse(this.responseStream);
   	        responseXml = document.getDocumentElement();

    	 } catch(Exception e) {
 			error(" ", e);
    	 } finally {
    		 closeExistingConnection();
    	 }

    	 return responseXml;
     }
     
     /**
      * Make an HTTP GET request to the given endpoint.
     * @throws BigCommerceException 
      */
	@SuppressWarnings({ "rawtypes" })
	public List serviceGET(String path, String data, IBigCommerceParser parser) throws BigCommerceException {

		OutputStreamWriter post = null;
		BufferedReader reader = null;
		HttpURLConnection conn = null;
		try {
			URL url = new URL(this.storeUrl + path);
			inform("URL: " + url.toString());
			
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			//conn.setRequestProperty("Content-Type", "application/json");
			//conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			//conn.setRequestProperty("X-Auth-Client", this.username);
			//conn.setRequestProperty("X-Auth-Token", this.apikey);
			conn.setRequestProperty("Authorization", getBasicAuthHeader());
			
			int responseCode = conn.getResponseCode();
			if(responseCode != 200) {
			    responseData = conn.getResponseCode() + " " + conn.getResponseMessage();
				inform("Response Data: " + responseData);
				return new ArrayList();
			}
			//Authenticator.setDefault (new BasicAuthenticator(this.username, this.apikey));
			//CustomerJsonParser.parse(conn.getInputStream());
			
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

			/*
			StringBuilder response = new StringBuilder();
			
			String inputLine;
			while ((inputLine = reader.readLine()) != null) {
				response.append(inputLine);
			}
			
			System.out.println(response.toString());
			*/
			return parser.parse(reader);
			

		} catch (Exception e) {
			responseData = e.getMessage();
			error(" ", e);
			throw new BigCommerceException(e);
		} finally {
			if (post != null) {
				try {
					post.close();
				} catch (Exception e) {
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
				}
			}
			if (conn != null) {
				try {
					conn.disconnect();
				} catch (Exception e) {
				}
			}
		}
	}
     
    /**
     * Make an HTTP POST request to the given endpoint.
     * @throws BigCommerceException 
     */
	public String servicePost(String path, String data) throws BigCommerceException {

		OutputStreamWriter post = null;
		BufferedWriter output = null;
		BufferedReader input = null;
		HttpURLConnection conn = null;
		try {
			URL url = new URL(this.storeUrl + path);
			inform("URL: " + url.toString());
			
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Content-Type", "application/json");
			//conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			//conn.setRequestProperty("X-Auth-Client", this.username);
			//conn.setRequestProperty("X-Auth-Token", this.apikey);
			conn.setRequestProperty("Authorization", getBasicAuthHeader());
			
			//int responseCode = conn.getResponseCode();
			//inform("Response Code: " + responseCode);
			//Authenticator.setDefault (new BasicAuthenticator(this.username, this.apikey));
			
			conn.setDoOutput(true);

			post = new OutputStreamWriter(conn.getOutputStream());
			output = new BufferedWriter(post);
			output.write(data);
			output.flush();
			output.close();
			
			int responseCode = conn.getResponseCode();
			if(responseCode != 201) {
			    responseData = conn.getResponseCode() + " " + conn.getResponseMessage();
			    
				inform("Error,Response Data: " + responseData);
				return "";
			}

			StringBuilder response = new StringBuilder();
			
			input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			while ((inputLine = input.readLine()) != null) {
				response.append(inputLine);
			}
			
			return response.toString();
		} catch (IOException e) {
			responseData = e.getMessage();
			error(" ", e);
			throw new BigCommerceException(e);
		} finally {
			if (post != null) {
				try {
					post.close();
				} catch (Exception e) {
				}
			}
			if (output != null) {
				try {
					output.close();
				} catch (Exception e) {
				}
			}
			if (input != null) {
				try {
					input.close();
				} catch (Exception e) {
				}
			}
			if (conn != null) {
				try {
					conn.disconnect();
				} catch (Exception e) {
				}
			}
		}
	}
	
	
    public String getResponseData() {
		return responseData;
	}

 	private void inform(String message) {
		//System.out.println(message);
		
		log.info("[BC] " + message);
	}
	
	private void error(String message, Throwable t) {
		//System.out.println(message);
		if( t != null) {
			log.error("[BC] ", t);
		} else {
			log.error("[BC] ");			
		}
	}
	
	public List<Product> getProduct(String param) throws BigCommerceException {
		
		String opPath = "products";
		if(param != null && !"".equals(param)) {
			 opPath += param;
		}
		return serviceGET(opPath, "", new ProductJsonParser());
		
	}

	public List<Option> getProductOption(String productId) throws BigCommerceException {
		
		String opPath = "products/" + productId + "/options";
		return serviceGET(opPath, "", new OptionJsonParser());
		
	}

	public List<Option> getProductOptionById(String productId, String optionId) throws BigCommerceException {
		
		String opPath = "products/" + productId + "/options/" + optionId;
		return serviceGET(opPath, "", new OptionJsonParser());
		
	}

	public List<OptionValue> getOptionValueById(String optionId, String valueId) throws BigCommerceException {
		
		String opPath = "options/" + optionId + "/values/" + valueId;
		return serviceGET(opPath, "", new OptionValueJsonParser());
		
	}

	public List<OptionSet> getProductOptionSet(String optionSetId) throws BigCommerceException {
		
		String opPath = "option_sets/" + optionSetId + "/options";
		return serviceGET(opPath, "", new OptionSetJsonParser());
		
	}

	public List<Sku> getProductSku(String productId) throws BigCommerceException {
		
		String opPath = "products/" + productId + "/skus";
		return serviceGET(opPath, "", new SkuJsonParser());
		
	}

	public static void main(String[] args)  {
    	 //String storeUrl = "https://shop.rocktape.com/api/v2/";
    	 //String username = "n_dev_test";
    	 //String apikey = "ac73596fa8c0fa0df0ef849fbb00399ca284589d";
		 // "https://store-r5adzn.mybigcommerce.com/api/v2";
		 // "thomas_hii";
		 // "ddee9404ad8c2f5698dae9270879fd7d3abb78ac";
		 String storeUrl = "https://store-atitqxiw.mybigcommerce.com/api/v2/";
		 String username = "neural-test";
		 String apikey = "fb2392bc81b6f46a89f027dcc76cbf401abc1ef9";
   	 
    	 BigCommerceConnector conn = new BigCommerceConnector(storeUrl, username, apikey);
    	 
    	 String data = "";    	 
    	 //String path = "/customers";
    	 //conn.bcGet(path, data, new CustomerJsonParser());
    	 
    	 //String path = "/products";
    	 //conn.bcGet(path, data, new ProductJsonParser());
    	 
    	 String path = "products";
    	 List<Customer> recList;
		try {
			recList = conn.serviceGET(path, data, new ProductJsonParser());

			if(!recList.isEmpty()) {
	        	 System.out.println("Customer: " + recList.size());
	        	 //System.out.println("Customer Count: " + recList);    		 
	    	 }
		} catch (BigCommerceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	 
     }
}
