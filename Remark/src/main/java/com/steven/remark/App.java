package com.steven.remark;

import com.steven.remark.common.Const;
import com.steven.remark.service.ExcuteCommentService;


public class App {

	public static void main(String[] args) {
		System.out.println("remove comment start ----->");
		
		ExcuteCommentService service = new ExcuteCommentService();
		
		service.processRoot(Const.FILE_IN_PATH);
		
		System.out.println("remove comment finish ----->");
	}
	
}
