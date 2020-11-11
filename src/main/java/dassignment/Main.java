/*******************************************************************************
 * Copyright (c) 2018 Lei Zhang.
 ******************************************************************************/

package dassignment;

import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

class Main
{
	public static void main(String[] args) throws EncryptedDocumentException, InvalidFormatException, IOException
	{
		new Solution().process();
	}
}
