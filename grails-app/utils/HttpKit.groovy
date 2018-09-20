import org.apache.http.Consts
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.NameValuePair
import org.apache.http.StatusLine
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory

import javax.servlet.http.HttpServletRequest

/**
 * Created by Administrator on 20/9/2018.
 */
class HttpKit {

    /**
     * 发送GET请求
     * @param url           请求地址
     * @param params        参数
     * @param headers       请求头
     * @return
     */
    static String doGet(String url,Map params=[:],Map<String,String> headers=[:]) {
        try {
            //创建一个默认的client实例
            CloseableHttpClient client = HttpClients.createDefault()
            // 添加请求参数
            String urlWithParams = url + "?" + setUrlParams(params)
            //发送get请求
            HttpGet request = new HttpGet(urlWithParams)
            // 设置请求头
            setHeaders(request,headers)

            def response = client.execute(request)

            /**请求发送成功，并得到响应**/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /**读取服务器返回过来的json字符串数据**/
                String strResult = EntityUtils.toString(response.getEntity())

                return strResult
            }
        }catch (Exception e) {
            def logger =LoggerFactory.getLogger(this.class)
            logger.error("发送GET请求失败，请求地址：${url}，参数：${params}")
        }
        return null
    }
    /**
     * 发送xml参数的post请求
     * @param url           请求地址
     * @param xmlString       xml数据
     * @return
     */
    static String doPostXml(String url,String xmlString) {
        try {
            //创建一个默认的client实例
            CloseableHttpClient client = HttpClients.createDefault()
            //发送post请求
            HttpPost request = new HttpPost(url)
            StringEntity stringEntity = new StringEntity(xmlString,"UTF-8")
            stringEntity.setContentEncoding("UTF-8")
            request.setEntity(stringEntity)
            request.setHeader("Content-Type","text/xml;charset=UTF-8")
//            setHeaders(request,["Content-Type": "text/html;charset=UTF-8"])
            HttpResponse response = client.execute(request)
            /**请求发送成功，并得到响应**/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /**读取服务器返回过来的json字符串数据**/
                String strResult = EntityUtils.toString(response.getEntity())

                return strResult
            }
        }catch (Exception e) {
            def logger =LoggerFactory.getLogger(this.class)
            logger.error("发送GET请求失败，请求地址：${url}，参数：${xmlString}")
        }
        return null
    }
    /**
     * post请求(用于key-value格式的参数)
     * @param url
     * @param params
     * @param headers
     * @param body
     * @return
     */
    static String doPost(String url,Map params=[:],Map headers=[:],String body=null){
        HttpResponse response
        CloseableHttpClient client
        try {
            //创建一个默认的client实例
            client = HttpClients.createDefault()
            // 实例化HTTP方法
            HttpPost request = new HttpPost()
            request.setURI(new URI(url))

            // 设置请求头
            setHeaders(request,headers)
            // 设置参数
            setParams(request,params)
            if(body){
                setBody(request,body)
            }

            response = client.execute(request)
            StatusLine status = response.getStatusLine()
            int state = status.getStatusCode()
            HttpEntity responseEntity = response.getEntity()
            String jsonString = EntityUtils.toString(responseEntity)
            if (state != HttpStatus.SC_OK) {
                def logger =LoggerFactory.getLogger(this.class)
                logger.error("发送POST请求返回状态非200，原因：${jsonString}，请求地址：${url}，参数：${params}")
            }
            println jsonString
            jsonString
        }catch(Exception e){
            def logger =LoggerFactory.getLogger(this.class)
            logger.error("发送POST请求失败，原因：${e.message}，请求地址：${url}，参数：${params}")
        }finally {
            if (response != null) {
                try {
                    response.close()
                } catch (IOException e) {
                    def logger =LoggerFactory.getLogger(this.class)
                    logger.error("发送POST请求关闭CloseableHttpResponse失败，请求地址：${url}，参数：${params}")
                }
            }
            try {
                client.close()
            } catch (IOException e) {
                def logger =LoggerFactory.getLogger(this.class)
                logger.error("发送POST请求关闭CloseableHttpClient失败，请求地址：${url}，参数：${params}")
            }
        }
    }

    /**
     * post请求（用于请求json格式的参数）
     * @param url               请求地址
     * @param params            请求参数
     * @param headers           请求头
     * @param body              请求体
     * @return
     */
    static String doPost(String url, String params,Map headers=[:],String body=null) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault()
        HttpPost httpPost = new HttpPost(url)
        headers.put("Accept", "application/json")
        headers.put("Content-Type", "application/json")
        // 设置请求头
        setHeaders(httpPost,headers)
        setParams(httpPost,params)
        if(body){
            setBody(httpPost,body)
        }
        CloseableHttpResponse response = null

        try {
            response = httpclient.execute(httpPost)
            StatusLine status = response.getStatusLine()
            int state = status.getStatusCode()
            if (state == HttpStatus.SC_OK) {
                HttpEntity responseEntity = response.getEntity()
                String jsonString = EntityUtils.toString(responseEntity)
                return jsonString
            }
            def logger =LoggerFactory.getLogger(this.class)
            logger.error("发送POST请求返回状态非200，请求地址：${url}，参数：${params}")
            return null
        }catch(Exception e){
            def logger =LoggerFactory.getLogger(this.class)
            logger.error("发送POST请求失败，请求地址：${url}，参数：${params}")
        }finally {
            if (response != null) {
                try {
                    response.close()
                } catch (IOException e) {
                    def logger =LoggerFactory.getLogger(this.class)
                    logger.error("发送POST请求关闭CloseableHttpResponse失败，请求地址：${url}，参数：${params}")
                }
            }
            try {
                httpclient.close()
            } catch (IOException e) {
                def logger =LoggerFactory.getLogger(this.class)
                logger.error("发送POST请求关闭CloseableHttpClient失败，请求地址：${url}，参数：${params}")
            }
        }
    }
    //添加请求头
    private static setHeaders(request,Map headers=[:]){
        headers?.each{ entry ->
            request.setHeader(entry.key,entry.value)
        }
    }
    // 添加GET请求参数，返回参数字符串
    private static String setUrlParams(Map params=[:]){
        List<NameValuePair> paramsList = []
        params.each{ entry ->
            paramsList.add(new BasicNameValuePair(entry.key,entry.value))
        }
        EntityUtils.toString(new UrlEncodedFormEntity(paramsList,Consts.UTF_8))
    }
    // 添加非GET请求参数
    private static setParams(request,Map params){
        List<NameValuePair> nvps = []
        params.each{ entry ->
            nvps.add(new BasicNameValuePair(entry.key,entry.value))
        }
        request.setEntity(new UrlEncodedFormEntity(nvps,Consts.UTF_8))
    }
    // 添加非GET请求参数
    private static setParams(request,String params){
        StringEntity entity = new StringEntity(params, Consts.UTF_8)
        request.setEntity(entity)
    }
    // 添加非GET请求参数body
    private static setBody(request,String body){
        HttpEntity entity = new StringEntity(body, ContentType.APPLICATION_JSON);
        request.setEntity(entity)
    }
    /**
     * 获取request的body内容
     * @param request
     * @return
     */
    static String getRequestBody(HttpServletRequest request) {
        InputStream is = null
        try {
            is = request.getInputStream()
            StringBuilder sb = new StringBuilder()
            byte[] b = new byte[4096]
            for (int n; (n = is.read(b)) != -1;)
            {
                sb.append(new String(b, 0, n))
            }
            def result = sb.toString()
            request.setAttribute("org.codehaus.groovy.grails.CACHED_XML_REQUEST_CONTENT", result)
            return result
        }
        catch (Exception e)
        {
            throw e
        }
        finally
        {
            if (null != is)
            {
                try
                {
                    is.close()
                }
                catch (IOException e){}
            }
        }

    }
}
