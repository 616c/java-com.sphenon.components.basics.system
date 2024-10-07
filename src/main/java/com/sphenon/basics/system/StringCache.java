package com.sphenon.basics.system;

/****************************************************************************
  Copyright 2001-2024 Sphenon GmbH

  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  License for the specific language governing permissions and limitations
  under the License.
*****************************************************************************/

import com.sphenon.basics.context.*;
import com.sphenon.basics.context.classes.*;
import com.sphenon.basics.configuration.*;
import com.sphenon.basics.exception.*;
import com.sphenon.basics.message.*;
import com.sphenon.basics.notification.*;
import com.sphenon.basics.customary.*;

import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.util.Date;
import java.text.SimpleDateFormat;

import java.io.*;

/*
    [Issue:EMOSAPPStringCache] [...emos...DockerProjectVolumeImage.template...].configuration.template,deploy.bash,StringCache.java

    Improvements:
    - separate read and write cache, so that read can be pre-deployed in jar
    - allow different runtimes to write simultaneously to same cache
    - solution might be:
      - introduce configurable 'number ranges', say 100-10000 Numbers
      - app allocates a number range ('reserved')
      - app uses number range to generate new texts
      - if stringcache based files are written out (COCP..., templates?),
        string-cache is flushed (not only on exit)
      - possibly introduce several string-cache files, and a
        string-cache index file for allocating ranges etc.
      - this would also require a lock file to sync multiple apps
    - Infos on locks:
      - https://docs.oracle.com/en/java/javase/13/docs/api/java.base/java/nio/channels/FileLock.html
      - https://docs.oracle.com/en/java/javase/13/docs/api/java.base/java/nio/channels/FileChannel.html#lock()
      - import java.io.*;
        import java.nio.*;
        RandomAccessFile file = null;
        FileChannel f = null;
        FileLock lock = null;
        try {
            File lockfile = new File(System.getProperty("java.io.tmpdir"), "my.lock");
            file = new RandomAccessFile(lockfile, "rw");
            f = file.getChannel();
            lock = f.tryLock(); // oder .lock();
            if (lock != null) {
                lockfile.deleteOnExit();
                f.write(...something...);
                f.force(false);
            } else { f.read(...if...you...like...); }
            ...do...something...
        } finally {
            if (lock != null && lock.isValid()) { lock.release(); }
            if (file != null){ file.close(); }
        }
*/


public class StringCache {
    static final public Class _class = StringCache.class;

    static protected long notification_level;
    static public    long adjustNotificationLevel(long new_level) { long old_level = notification_level; notification_level = new_level; return old_level; }
    static public    long getNotificationLevel() { return notification_level; }
    static { notification_level = NotificationLocationContext.getLevel(_class); };

    static protected Configuration config;
    static { config = Configuration.create(RootContext.getInitialisationContext(), _class); };

    protected String cache;
    
    protected StringCache(CallContext context) {

        if (config.get(context, "LoadCache", false)) {
            this.loadCache(context);
        }

        if (this.timestamp == null) {
            this.timestamp = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")).format(new Date());
        }
        
        if (this.texts == null) {
            this.texts = new ArrayList<String>();
            this.texts.add(null);
            this.texts.add("");
        }

        this.saveCacheOnExit(context);
    }

    static volatile protected StringCache singleton;

    static public StringCache getSingleton(CallContext context) {
        if (singleton == null) {
            synchronized(StringCache.class) {
                if (singleton == null) {
                    singleton = new StringCache(context);
                }
            }
        }
        return singleton;
    }

    protected String timestamp;
    protected List<String> texts;
    protected Map<String,Integer> map;

    public String getText(CallContext context, int index) {
        return this.texts.get(index);
    }

    public String getTimestamp(CallContext context) {
        return this.timestamp;
    }

    public void checkTimestamp(CallContext context, String timestamp_to_check, String client) {
        if (this.timestamp == null || this.timestamp.equals(timestamp_to_check) == false) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, "Timestamp '%(timestamp)' from string cache '%(cache)' does not match clients '%(client)' timestamp '%(clientstamp)'", "timestamp", this.timestamp, "cache", this.cache == null || this.cache.isEmpty() ? "[not configured]" : this.cache, "client", client, "clientstamp", timestamp_to_check);
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    public synchronized int putText(CallContext context, String text) {
        if (text == null) { return 0; }
        if (this.map == null) {
            this.map = new HashMap<String,Integer>();
            int i=0;
            for (String t : this.texts) {
                this.map.put(t, i++);
            }
        }
        Integer index = this.map.get(text);
        if (index == null) {
            if (do_save_cache == false) {
                CustomaryContext.create((Context)context).throwConfigurationError(context, "String cache is used, but not configured for save on exit");
                throw (ExceptionConfigurationError) null; // compiler insists
            }
            index = this.texts.size();
            this.texts.add(text);
            this.map.put(text, index);
        }
        return index;
    }

    protected boolean do_save_cache;

    public void saveCacheOnExit(CallContext context) {
        this.do_save_cache = config.get(context, "SaveCacheOnExit", false);
        java.lang.Runtime.getRuntime().addShutdownHook(new Thread() { public void run() { saveCache(RootContext.getDestructionContext()); } });
    }

    public void saveCache(CallContext context) {
        if (do_save_cache) {
            if (this.map == null) {
                return; // no need to save
            }

            if (this.cache == null) {
                return; // no place to save
            }

            try {
                File f = new File(this.cache);
                f.setWritable(true);
                FileOutputStream fos = new FileOutputStream(f);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(this.timestamp);
                oos.writeObject(this.texts);
                oos.close();
                fos.close();
            } catch (IOException ioe) {
                CustomaryContext.create((Context)context).throwEnvironmentFailure(context, ioe, "Cannot save string cache to '%(file)'", "file", this.cache);
                throw (ExceptionEnvironmentFailure) null; // compiler insists
            }
        }
    }

    public void loadCache(CallContext context) {
        this.cache = config.get(context, "Cache", (String) null);
        if (this.cache == null) {
            return; // no place to load from
        }

        try {
            InputStream is = null;
            if (this.cache.startsWith("//JavaResource/"))  {
                String scr = this.cache.substring(cache.length() > 15 && this.cache.charAt(15) == '/' ? 16 : 15);
                is = this.getClass().getClassLoader().getResourceAsStream(scr);
                if (is == null) {
                    if ((notification_level & Notifier.MONITORING) != 0) { NotificationContext.sendCaution(context, "String cache configured ('%(cache)'), but not found, this may cause trouble lateron", "cache", this.cache); }
                    return; // nothing to load
                }
            } else {
                File f = new File(this.cache);
                if (f.exists() == false) {
                    if ((notification_level & Notifier.MONITORING) != 0) { NotificationContext.sendCaution(context, "String cache configured ('%(cache)'), but not found, this may cause trouble lateron", "cache", this.cache); }
                    return; // nothing to load
                }
                FileInputStream fis = new FileInputStream(f);
                is = fis;
            }

            ObjectInputStream ois = new ObjectInputStream(is);
            this.timestamp = (String) ois.readObject();
            this.texts = (List<String>) ois.readObject();
            ois.close();
            is.close();
        } catch (IOException ioe) {
            CustomaryContext.create((Context)context).throwEnvironmentFailure(context, ioe, "Cannot load string cache from '%(file)'", "file", this.cache);
            throw (ExceptionEnvironmentFailure) null; // compiler insists
        } catch (ClassNotFoundException cnfe) {
            CustomaryContext.create((Context)context).throwInvalidState(context, cnfe, "Cannot load string cache from '%(file)', class unexpectedly not found (resource should contain List<String>)", "file", this.cache);
            throw (ExceptionInvalidState) null; // compiler insists
        }
    }
}
