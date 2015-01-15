package ODMonitor.App.file;

import java.io.IOException;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

public class file_operate_bmp extends file_operate_byte_array {
	private String Tag = "file_operate_bmp";
	protected String file_extension = "png";
	
	public file_operate_bmp(String dir_name, String file_name, String file_extension) {
		super(dir_name, file_name, false);
		this.file_extension = file_extension;
		// TODO Auto-generated constructor stub
	}
	
	/* file naming format logyyyymmdd-hhmmss.txt*/
	@Override
	public String generate_filename() {
	    String filename;
		filename = CreateFileName + df.format(new Date()) + "." + file_extension;
		Log.d(Tag, filename);
		return filename;
	}
		
	@Override
	public String generate_filename_no_date() {
		String filename;
		filename = CreateFileName + "." + file_extension;
		Log.d(Tag, filename);
		return filename;
	}

	public void write_file(Bitmap bitmap, CompressFormat format, int quality) {
		if (fos != null) {
			try {
			    bitmap.compress(format, quality, fos);
			} catch (Exception e) {
			    e.printStackTrace();
			}
		}
	}
}
