/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.iforms.user.custLoanDet;

import com.newgen.iforms.common.LogMessages;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.user.custLoanDet.Request.CreditWorkflowAPI;
import com.newgen.iforms.user.custLoanDet.Request.FKLCREDITWORKFLOWAPIType;
import com.newgen.iforms.user.custLoanDet.Request.WebRequestCommon;
import com.newgen.iforms.user.custLoanDet.Request.enquiryInputCollection;
import com.newgen.iforms.user.custLoanDet.Response.CustLoanDetResponses;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXB;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author User
 */
public class CustLoanDet {
    //public IFormReference ifr;
//    public CustLoanDet(){
//         this.ifr=ifr;
//    }
    
    
    
    
    
    
    
    

    public String getCustLoanDetRequest(String stringData,IFormReference ifr) {
       
        StringBuilder Return = new StringBuilder();
        

        try {
            LogMessages.statusLog.info("Inside getCustLoanDetRequest");
//        	Date n = new Date();
//            DateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm");
//            String b = stringData.split("#",-1)[1].substring(0,3);


            String rawXML = createXML(ifr);
            Return.append(createXMLwithSOAP(rawXML));

        } catch (Exception e) {
            LogMessages.errorLog.info("Exception inside getCustLoanDetRequest::", e);
        }
        LogMessages.xmlLog.info(String.valueOf(Return));
        return String.valueOf(Return);
    }

   public String getLoanProductCodeResponse(String processName, String response,IFormReference ifr) {

        //, CustomerId = "", Loanid = "", LoanCategory = "", LoanType = "", TotalCommittment = "", Loanbalance = "", RepaymentAmount = "", DueDate = "", Term = "", InterestRate = "", OverdueAmount = "", OverdueStatus = "", OverdueDays = "", PostingRestrict = "", Writeoff = "";
        LogMessages.statusLog.info("Inside getCustLoanDetResponse#####################" + response);
        List<String> ll = new ArrayList<>();
        JSONArray jsonArray = new JSONArray();
        String result="";

        String status = "";//, CustomerId = "", Loanid = "", LoanCategory = "", LoanType = "", TotalCommittment = "", Loanbalance = "", RepaymentAmount = "", DueDate = "", Term = "", InterestRate = "", OverdueAmount = "", OverdueStatus = "", OverdueDays = "", PostingRestrict = "", Writeoff = "";

        try {
            // Initialize document builder to parse XML
            //LogMessages.statusLog.info("Inside getLoanProductCodeResponse");
            JSONObject jsonobject=new JSONObject();
            //JSONArray jsonarray=new JSONArray();
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            
            InputSource is = new InputSource(new StringReader(response));
            Document doc = dBuilder.parse(is);

            // Normalize the document
            doc.getDocumentElement().normalize();

            // Extract successIndicator
            NodeList statusList = doc.getElementsByTagName("successIndicator");
            if (statusList.getLength() > 0) {
                status = statusList.item(0).getTextContent();

            }
            if (status.equalsIgnoreCase("Success")) {

                // Extract the ns45:CUSTID element
                NodeList custIdNodes = doc.getElementsByTagNameNS("*","CUSTID");
                for (int i = 0; i < custIdNodes.getLength(); i++) {
            Element custIdElement = (Element) custIdNodes.item(i);
            String custIdText = custIdElement.getTextContent();
            
            JSONObject json = new JSONObject();
            String[] parts = custIdText.split("\\*");
            Map<String, String> keyMap = getKeyMap();
            for (String part : parts) {
                String[] keyValue = part.split(":");
                if (keyValue.length == 2) {
                    String key = keyMap.getOrDefault(keyValue[0], keyValue[0]);
                    String value=keyValue[1];
                    if(key.equals("Due Date")){
                        value=formatteddate(value);
                    }
                    json.put(key, value);
                }
            }
            jsonArray.add(json);
            
            
            
        }
                LogMessages.statusLog.info(jsonArray);
                ifr.addDataToGrid("OtherLoansInFaulu", jsonArray);
            result="Success";
       }
        }catch(Exception e){
            LogMessages.errorLog.info("Exception in try block of getLoanProductCodeResponse"+e);
           result="Failure";
       }
        return result;
    }
    public static String formatteddate(String value){
        if (value != null && value.length() == 8) {
            //20240710
            String day=value.substring(6,8);
            String month = value.substring(4, 6);
            String year = value.substring(0,4);
            
            String formattedDate =day+"/"+ month + "/" + year;
            return formattedDate;
        } else {
            return value;
        }
        
        
       
    }

    private static Map<String, String> getKeyMap() {
        Map<String, String> keyMap = new HashMap<>();
        keyMap.put("CustomerName", "Customer Name");
        keyMap.put("CustomerId", "Customer Id");
        keyMap.put("Loanid", "AA Id");
        keyMap.put("LoanCategory", "Product Code");
        keyMap.put("LoanType", "Product Name");
        keyMap.put("TotalCommittment", "Total Commitment");
        keyMap.put("Loanbalance", "Loan Balance");
        keyMap.put("RepaymentAmount", "Repayment Amount");
        keyMap.put("DueDate", "Due Date");
        keyMap.put("Term", "Term");
        keyMap.put("InterestRate", "Interest Rate");
        keyMap.put("OverdueAmount", "Overdue Amount");
        keyMap.put("OverdueStatus", "Overdue Status");
        keyMap.put("OverdueDays", "Overdue Days");
        keyMap.put("PostingRestrict", "Posting Restricts");
        keyMap.put("Writeoff", "Write Off");
        return keyMap;
    }

    public String createXML(IFormReference ifr) {
        
//IFormReference ifr=new IFormReference();
        enquiryInputCollection enquiry = new enquiryInputCollection();
        
        enquiry.columnName = "CUST.ID";
        enquiry.criteriaValue = ifr.getValue("PersonalInformation_EXT_Q_KraPin").toString();//"597456";
        enquiry.operand = "EQ";

        WebRequestCommon webreq = new WebRequestCommon();
        webreq.company = "";
        webreq.password = "POLice123";
        webreq.userName = "H2HUSER";

        FKLCREDITWORKFLOWAPIType fkl = new FKLCREDITWORKFLOWAPIType();
        fkl.enquiryInputCollection = enquiry;

        CreditWorkflowAPI credit = new CreditWorkflowAPI();
        credit.WebRequestCommon = webreq;
        credit.FKLCREDITWORKFLOWAPIType = fkl;

        StringWriter stringwriter = new StringWriter();

        JAXB.marshal(credit, stringwriter);

        //marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        String xmlString = stringwriter.toString();
        int index = xmlString.indexOf("?>");
        if (index != -1) {
            xmlString = xmlString.substring(index + 2).trim();
        }

        return xmlString;

    }

    public String createXMLwithSOAP(String data) {
        StringBuilder sb = new StringBuilder();
        String raw = data.replaceAll("creditWorkflowAPI", "faul:CreditWorkflowAPI");

        // Split the raw XML into lines
        String[] lines = raw.split("\n");
        StringBuilder indentedRaw = new StringBuilder();

        // Add a tab character to each line
        for (String line : lines) {
            indentedRaw.append("\t").append(line).append("\n");
        }

        String xmlString1 = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:faul=\"http://temenos.com/FauluCreditWorkflow\">\n"
                + "   <soapenv:Header/>\n"
                + "   <soapenv:Body>\n";
        String xmlString2 = "   </soapenv:Body>\n"
                + "</soapenv:Envelope>";

        sb.append(xmlString1).append(indentedRaw).append(xmlString2);

        return sb.toString();

    }
    
    
    
    
    
    //for testing purpose
    public String createXML(String id,IFormReference ifr) {
        
//IFormReference ifr=new IFormReference();
        enquiryInputCollection enquiry = new enquiryInputCollection();
        
        enquiry.columnName = "CUST.ID";
        enquiry.criteriaValue = ifr.getValue("PersonalInformation_EXT_Q_KraPin").toString();//"597456";
        enquiry.operand = "EQ";

        WebRequestCommon webreq = new WebRequestCommon();
        webreq.company = "";
        webreq.password = "POLice123";
        webreq.userName = "H2HUSER";

        FKLCREDITWORKFLOWAPIType fkl = new FKLCREDITWORKFLOWAPIType();
        fkl.enquiryInputCollection = enquiry;

        CreditWorkflowAPI credit = new CreditWorkflowAPI();
        credit.WebRequestCommon = webreq;
        credit.FKLCREDITWORKFLOWAPIType = fkl;

        StringWriter stringwriter = new StringWriter();

        JAXB.marshal(credit, stringwriter);

        //marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        String xmlString = stringwriter.toString();
        int index = xmlString.indexOf("?>");
        if (index != -1) {
            xmlString = xmlString.substring(index + 2).trim();
        }

        return xmlString;

    }


}
