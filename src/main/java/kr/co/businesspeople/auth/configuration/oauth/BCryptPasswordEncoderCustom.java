package kr.co.businesspeople.auth.configuration.oauth;

import java.security.MessageDigest;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author ykleem
 * @date   2017. 7. 14.
 * @Desc   
 */
public class BCryptPasswordEncoderCustom extends BCryptPasswordEncoder
{
	@Override
	public String encode(CharSequence rawPassword) {
		/*System.out.println("rawPassword" + rawPassword.toString());*/
		String result = "";
		try {
			result = pwdSHA256(rawPassword.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        return result;
	}
	
	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		boolean result = false;
		
		if(rawPassword != null && !"".equals(rawPassword) 
				&& encodedPassword != null && !"".equals(encodedPassword))
		{
			if(encode(rawPassword).equals(encodedPassword))
			{
				result = true;
			}
		}
		return result;
	}
	
	private String pwdSHA256(String str) throws Exception {
		MessageDigest sh = MessageDigest.getInstance("SHA-256"); 
		sh.update(str.getBytes()); 
		byte byteData[] = sh.digest();
		StringBuffer sb = new StringBuffer(); 
		for(int i = 0 ; i < byteData.length ; i++){
			sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}
}