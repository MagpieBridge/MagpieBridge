package magpiebridge.util;

import java.util.Stack;
import com.google.gson.JsonObject;
/**
 * JsonFormatHandler provides methods to handle Json data
 *  
 * @author samic
 *
 */
public class JsonFormatHandler {
	
	 /**
	   * Keep only JSON part of the string
	   *
	   * @param str
	   * @return
	   */
	  public static String getJsonFromString(String str) {
	    String content = "";
	    String temp = "";
	    Stack<Character> stack = new Stack<Character>();
	    for (char singleChar : str.toCharArray()) {
	      if (stack.isEmpty() && singleChar == '{') {
	        stack.push(singleChar);
	        temp += singleChar;
	      } else if (!stack.isEmpty()) {
	        temp += singleChar;
	        if (singleChar == '}' && stack.peek().equals('{')) {
	          stack.pop();
	          if (stack.isEmpty()) {
	            content += temp;
	            temp = "";
	          }
	        } else if (singleChar == '{' || singleChar == '}') {
	          stack.push(singleChar);
	        }
	      } else if (temp.length() > 0 && stack.isEmpty()) {
	        content += temp;
	        temp = "";
	      }
	    }
	    return content;
	  }
	  /**
	   * Check whether object is not null and it has the property
	   * 
	   * @param obj
	   * @param property
	   * @return
	   */
	  public static boolean notNullAndHas(JsonObject obj, String property) {
		    return obj != null && obj.has(property);
		  }
}
