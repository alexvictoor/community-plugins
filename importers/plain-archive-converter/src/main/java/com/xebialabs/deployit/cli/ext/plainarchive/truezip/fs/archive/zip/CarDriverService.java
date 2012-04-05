package com.xebialabs.deployit.cli.ext.plainarchive.truezip.fs.archive.zip;

import de.schlichtherle.truezip.fs.FsDriver;
import de.schlichtherle.truezip.fs.FsScheme;
import de.schlichtherle.truezip.fs.archive.zip.JarDriver;
import de.schlichtherle.truezip.fs.spi.FsDriverService;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;

import java.util.Map;

import org.kohsuke.MetaInfServices;

/**
 * Maps the Client Application Archive (car) extension to the TrueZip JAR driver.
 */
@MetaInfServices
public class CarDriverService extends FsDriverService {
    private final Map<FsScheme, FsDriver> DRIVERS = 
        newMap(new Object[][] { { "car", new JarDriver(IOPoolLocator.SINGLETON) } });

    @Override
    public Map<FsScheme, FsDriver> get() {
        return DRIVERS;
    }
}
