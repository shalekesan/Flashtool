package org.simpleusblogger;

import java.io.IOException;
import java.util.HashSet;

import com.igormaznitsa.jbbp.JBBPParser;
import com.igormaznitsa.jbbp.io.JBBPBitInputStream;
import com.igormaznitsa.jbbp.mapper.Bin;

public class USBRecord {
	@Bin long irp;
	@Bin byte[] reserved1;
	@Bin int recordlength;
	@Bin byte[] reserved2;
	USBHeader header=null;
	USBHeaderExtended headerext=null;
	public int recnum=0;
	public byte[] data=null;
	
	

	public void parse(JBBPBitInputStream usbStream) throws IOException {
		JBBPParser USBHeaderParser = JBBPParser.prepare(
				"<short usb_Length;" +
				"<short usb_Function;" +
				"<int usb_Status;" +
				"<long usb_UsbDeviceHandle;" +
				"<long usb_UsbdFlags;" +
				"<long usb_PipeHandle;" +
				"<int usb_TransferFlags;" +
				"<int usb_TransferBufferLength;" +
				"<long usb_TransferBuffer;" +
				"<long usb_TransferBufferMDL;" +
				"<long usb_UrbLink;" +
				"<long usb_hcdendpoint;" +
				"<long usb_hcdirp;" +
				"<long usb_hcdlistentry;" +
				"<long usb_flink;" +
				"<long usb_blink;" +
				"<long usb_hcdlistentry2;" +
				"<long usb_hcdcurrentflushpointer;" +
				"<long usb_hcdextension;"
				);

		JBBPParser USBHeaderExtended = JBBPParser.prepare(
				"byte brmRequestType;" + 
				"byte bRequest;" + 
				"<short wValue;" + 
				"<short wIndex;" + 
				"<short wLength;"
				);
		
		if (recordlength>=128) {
			header = USBHeaderParser.parse(usbStream).mapTo(USBHeader.class);
		}
		else usbStream.skip(recordlength);
		try {
			if (header.usb_Length!=128) {
				header=null;
				usbStream.skip(recordlength-128);
			}
			else data = usbStream.readByteArray(recordlength-128);
		} catch (NullPointerException npe) {}
	}

	public byte[] getData() {
		return data;
	}
	public String toString() {
		return "Record : "+recnum+" length : "+recordlength+"\n   "+header+"\n read : "+data.length;
	}

}