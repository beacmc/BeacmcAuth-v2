package com.beacmc.beacmcauth.velocity.library;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.VelocityLibraryManager;
import com.beacmc.beacmcauth.api.library.LibraryProvider;
import com.beacmc.beacmcauth.velocity.VelocityBeacmcAuth;

public class VelocityLibraryProvider implements LibraryProvider {

    private final VelocityLibraryManager manager;

    public VelocityLibraryProvider() {
        VelocityBeacmcAuth velocityAuth = VelocityBeacmcAuth.getInstance();
        manager = new VelocityLibraryManager(velocityAuth, velocityAuth.getVelocityLogger(), velocityAuth.getDataDirectory(), velocityAuth.getVelocityProxyServer().getPluginManager());
        manager.addMavenCentral();
        manager.addSonatype();
        manager.addJitPack();
    }

    @Override
    public void loadLibrary(Library library) {
        manager.loadLibrary(library);
    }

    @Override
    public void loadLibraries(Library... libraries) {
        for (Library library : libraries) loadLibrary(library);
    }

    @Override
    public void addRepository(String url) {
        manager.addRepository(url);
    }
}
