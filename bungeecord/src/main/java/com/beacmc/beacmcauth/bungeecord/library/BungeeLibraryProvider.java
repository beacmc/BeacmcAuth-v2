package com.beacmc.beacmcauth.bungeecord.library;

import com.alessiodp.libby.BungeeLibraryManager;
import com.alessiodp.libby.Library;
import com.beacmc.beacmcauth.api.library.LibraryProvider;
import com.beacmc.beacmcauth.bungeecord.BungeeBeacmcAuth;

public class BungeeLibraryProvider implements LibraryProvider {

    private final BungeeLibraryManager manager;

    public BungeeLibraryProvider() {
        manager = new BungeeLibraryManager(BungeeBeacmcAuth.getInstance());
        manager.addMavenCentral();
        manager.addJitPack();
        manager.addSonatype();
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
