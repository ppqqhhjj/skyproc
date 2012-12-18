package skyproc;

import java.io.IOException;
import java.util.zip.DataFormatException;
import lev.LChannel;
import lev.LExporter;
import lev.Ln;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Justin Swanson
 */
public class SubStringNonNull extends SubString {

    SubStringNonNull(Type t) {
	super(t);
    }

    @Override
    void parseData(LChannel in) throws BadRecord, DataFormatException, BadParameter {
	in.skip(getIdentifierLength() + getSizeLength());
	string = Ln.arrayToString(in.extractInts(in.available()));
	if (logging()) {
	    logSync(getType().toString(), "Setting " + toString() + " to " + print());
	}
    }

    @Override
    int getContentLength(Mod srcMod) {
	return string.length();
    }

    @Override
    void export(LExporter out, Mod srcMod) throws IOException {
	out.write(getType().toString());
	out.write(getContentLength(srcMod), 2);
	out.write(string);
    }

    @Override
    SubRecord getNew(Type type_) {
	return new SubStringNonNull(type_);
    }
}