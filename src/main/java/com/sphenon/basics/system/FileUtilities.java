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
import com.sphenon.basics.exception.*;
import com.sphenon.basics.context.classes.*;
import com.sphenon.basics.message.*;
import com.sphenon.basics.notification.*;
import com.sphenon.basics.customary.*;

import java.io.*;
import java.nio.file.*;
import java.nio.channels.FileChannel;
import java.nio.CharBuffer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.List;
import java.util.Vector;
import java.util.Properties;
import java.util.regex.*;

public class FileUtilities {

    static public Vector<String> tryReadStream(CallContext context, InputStream stream) {
        return doReadStream(context, stream, false, "<stream>");
    }

    static public Vector<String> readStream(CallContext context, InputStream stream) {
        return doReadStream(context, stream, true, "<stream>");
    }

    static public Vector<String> tryReadStream(CallContext context, InputStream stream, String info) {
        return doReadStream(context, stream, false, info);
    }

    static public Vector<String> readStream(CallContext context, InputStream stream, String info) {
        return doReadStream(context, stream, true, info);
    }

    static public Vector<String> tryReadFile(CallContext context, String file_name) {
        return doReadFile(context, new File(file_name), false);
    }

    static public Vector<String> tryReadFile(CallContext context, String file_name, String include_regexp, String exclude_regexp) {
        return doReadFile(context, new File(file_name), false, include_regexp, exclude_regexp);
    }

    static public Vector<String> readFile(CallContext context, String file_name) {
        return doReadFile(context, new File(file_name), true);
    }

    static public Vector<String> readFile(CallContext context, String file_name, String include_regexp, String exclude_regexp) {
        return doReadFile(context, new File(file_name), true, include_regexp, exclude_regexp);
    }

    static public Vector<String> tryReadFile(CallContext context, File file) {
        return doReadFile(context, file, false);
    }

    static public Vector<String> tryReadFile(CallContext context, File file, String include_regexp, String exclude_regexp) {
        return doReadFile(context, file, false, include_regexp, exclude_regexp);
    }

    static public Vector<String> readFile(CallContext context, File file) {
        return doReadFile(context, file, true);
    }

    static public Vector<String> readFile(CallContext context, File file, String include_regexp, String exclude_regexp) {
        return doReadFile(context, file, true, include_regexp, exclude_regexp);
    }

    static public Vector<String> doReadFile(CallContext context, File file, boolean throw_exception) {
        return doReadFile(context, file, throw_exception, null, null);
    }

    static public Vector<String> doReadFile(CallContext context, File file, boolean throw_exception, String include_regexp, String exclude_regexp) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException fnfe) {
            if (throw_exception) {
                CustomaryContext.create((Context)context).throwPreConditionViolation(context, fnfe, "Cannot read file '%(filename)'", "filename", file.getPath());
                throw (ExceptionPreConditionViolation) null; // compiler insists
            } else {
                return null;
            }
        }
        return doReadStream(context, fis, throw_exception, include_regexp, exclude_regexp, file.getPath());
    }

    static public Vector<String> doReadStream(CallContext context, InputStream stream, boolean throw_exception, String info) {
        return doReadStream(context, stream, throw_exception, null, null, info);
    }

    static public Vector<String> doReadStream(CallContext context, InputStream stream, boolean throw_exception, String include_regexp, String exclude_regexp, String info) {
        try {
            InputStreamReader isr = new InputStreamReader(stream, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            Vector<String> lines = doReadReader(context, br, throw_exception, include_regexp, exclude_regexp, info);

            isr.close();
            stream.close();

            return lines;

        } catch (UnsupportedEncodingException uee) {
            if (throw_exception) {
                CustomaryContext.create((Context)context).throwPreConditionViolation(context, uee, "Cannot read from '%(info)'", "info", info);
                throw (ExceptionPreConditionViolation) null; // compiler insists
            } else {
                return null;
            }
        } catch (IOException ioe) {
            if (throw_exception) {
                CustomaryContext.create((Context)context).throwPreConditionViolation(context, ioe, "Cannot read from '%(info)'", "info", info);
                throw (ExceptionPreConditionViolation) null; // compiler insists
            } else {
                return null;
            }
        }
    }

    static public Vector<String> doReadReader(CallContext context, BufferedReader reader, boolean throw_exception, String info) {
        return doReadReader(context, reader, throw_exception, null, null, info);
    }

    static public Vector<String> doReadReader(CallContext context, BufferedReader reader, boolean throw_exception, String include_regexp, String exclude_regexp, String info) {
        try {

            Vector<String> lines = new Vector<String>();
            String line;
            while ((line = reader.readLine()) != null) {
                int len = line.length();
                line = ((len > 0 && line.charAt(len - 1) == '\n') ? line.substring(0, len - 1) : line);
                if (    (include_regexp == null || line.matches(include_regexp))
                     && (exclude_regexp == null || line.matches(exclude_regexp))
                   ) {
                    lines.add(line);
                }
            }

            reader.close();

            return lines;

        } catch (UnsupportedEncodingException uee) {
            if (throw_exception) {
                CustomaryContext.create((Context)context).throwPreConditionViolation(context, uee, "Cannot read from '%(info)'", "info", info);
                throw (ExceptionPreConditionViolation) null; // compiler insists
            } else {
                return null;
            }
        } catch (IOException ioe) {
            if (throw_exception) {
                CustomaryContext.create((Context)context).throwPreConditionViolation(context, ioe, "Cannot read from '%(info)'", "info", info);
                throw (ExceptionPreConditionViolation) null; // compiler insists
            } else {
                return null;
            }
        }
    }

    static public String doReadReaderIntoString(CallContext context, BufferedReader reader, boolean throw_exception, String info) {
        try {

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            reader.close();

            return result.toString();

        } catch (UnsupportedEncodingException uee) {
            if (throw_exception) {
                CustomaryContext.create((Context)context).throwPreConditionViolation(context, uee, "Cannot read from '%(info)'", "info", info);
                throw (ExceptionPreConditionViolation) null; // compiler insists
            } else {
                return null;
            }
        } catch (IOException ioe) {
            if (throw_exception) {
                CustomaryContext.create((Context)context).throwPreConditionViolation(context, ioe, "Cannot read from '%(info)'", "info", info);
                throw (ExceptionPreConditionViolation) null; // compiler insists
            } else {
                return null;
            }
        }
    }

    static public void tryWriteFile(CallContext context, String file_name, String... lines) {
        doWriteFile(context, new File(file_name), false, false, lines);
    }

    static public void writeFile(CallContext context, String file_name, String... lines) {
        doWriteFile(context, new File(file_name), true, false, lines);
    }

    static public void tryWriteFile(CallContext context, File file, String... lines) {
        doWriteFile(context, file, false, false, lines);
    }

    static public void writeFile(CallContext context, File file, String... lines) {
        doWriteFile(context, file, true, false, lines);
    }

    static public void tryAppendToFile(CallContext context, String file_name, String... lines) {
        doWriteFile(context, new File(file_name), false, true, lines);
    }

    static public void appendToFile(CallContext context, String file_name, String... lines) {
        doWriteFile(context, new File(file_name), true, true, lines);
    }

    static public void tryAppendToFile(CallContext context, File file, String... lines) {
        doWriteFile(context, file, false, true, lines);
    }

    static public void appendToFile(CallContext context, File file, String... lines) {
        doWriteFile(context, file, true, true, lines);
    }

    static public void tryWriteFile(CallContext context, String file_name, List<String> line_list) {
        doWriteFile(context, new File(file_name), false, false, line_list);
    }

    static public void writeFile(CallContext context, String file_name, List<String> line_list) {
        doWriteFile(context, new File(file_name), true, false, line_list);
    }

    static public void tryWriteFile(CallContext context, File file, List<String> line_list) {
        doWriteFile(context, file, false, false, line_list);
    }

    static public void writeFile(CallContext context, File file, List<String> line_list) {
        doWriteFile(context, file, true, false, line_list);
    }

    static public void tryAppendToFile(CallContext context, String file_name, List<String> line_list) {
        doWriteFile(context, new File(file_name), false, true, line_list);
    }

    static public void appendToFile(CallContext context, String file_name, List<String> line_list) {
        doWriteFile(context, new File(file_name), true, true, line_list);
    }

    static public void tryAppendToFile(CallContext context, File file, List<String> line_list) {
        doWriteFile(context, file, false, true, line_list);
    }

    static public void appendToFile(CallContext context, File file, List<String> line_list) {
        doWriteFile(context, file, true, true, line_list);
    }

    static public void doWriteFile(CallContext context, File file, boolean throw_exception, boolean append, String... lines) {
        doWriteFile(context, file, throw_exception, append, null, lines);
    }

    static public void doWriteFile(CallContext context, File file, boolean throw_exception, boolean append, List<String> line_list) {
        doWriteFile(context, file, throw_exception, append, line_list, (String[]) null);
    }

    static public void doWriteFile(CallContext context, File file, boolean throw_exception, boolean append, List<String> line_list, String... lines) {
        try {
            FileOutputStream fos = new FileOutputStream(file, append);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bw = new BufferedWriter(osw);
            PrintWriter pw = new PrintWriter(bw);

            if (lines != null) {
                for (String line : lines) {
                    pw.println(line);
                }
            }

            if (line_list != null) {
                for (String line : line_list) {
                    pw.println(line);
                }
            }

            pw.close();
            bw.close();
            osw.close();
            fos.close();

        } catch (FileNotFoundException fnfe) {
            if (throw_exception) {
                CustomaryContext.create((Context)context).throwPreConditionViolation(context, fnfe, "Cannot write to file '%(filename)'", "filename", file.getPath());
                throw (ExceptionPreConditionViolation) null; // compiler insists
            }
        } catch (UnsupportedEncodingException uee) {
            if (throw_exception) {
                CustomaryContext.create((Context)context).throwPreConditionViolation(context, uee, "Cannot write to file '%(filename)'", "filename", file.getPath());
                throw (ExceptionPreConditionViolation) null; // compiler insists
            }
        } catch (IOException ioe) {
            if (throw_exception) {
                CustomaryContext.create((Context)context).throwPreConditionViolation(context, ioe, "Cannot write to file '%(filename)'", "filename", file.getPath());
                throw (ExceptionPreConditionViolation) null; // compiler insists
            }
        }
    }

    static public boolean exists(CallContext context, String file_name) {
        if (file_name == null) { return false; }
        File file = new File(file_name);
        return file.exists();
    }

    static public void backupFile(CallContext context, File file, String postfix) {
        File backup = new File(file.getParent(), file.getName() + postfix);
        copyFile(context, file, backup);
    }

    static public void copyFile(CallContext context, File source, File target) {
        copyFile(context, source, target, false);
    }

    static public void copyFile(CallContext context, File source, File target, boolean with_attachments) {
        if (source.isFile()) {
            copy(context, source, target);
            if (with_attachments) {
                File source_parent = source.getParentFile();
                String attached_name = source.getName() + ".attached";
                File attached = new File(source_parent, attached_name);
                if (attached.exists()) {
                    Vector<String> attachments = tryReadFile(context, attached);
                    if (attachments != null) {
                        File target_parent = target.getParentFile();
                        copyFile(context, attached, new File(target_parent, attached_name), false);
                        for (String attachment : attachments) {
                            copyFile(context, new File(source_parent, attachment), new File(target_parent, attachment), false);
                        }
                    }
                }
            }
        }
    }

    static public void copy(CallContext context, String source, String target) {
        copy(context, new File(source), new File(target));
    }

    static public boolean copy(CallContext context, String source, String target, boolean throw_exception) {
        return copy(context, new File(source), new File(target), throw_exception);
    }

    static public void copy(CallContext context, String source, String target, String filter_file) {
        copy(context, new File(source), new File(target), new File(filter_file));
    }

    static public void copy(CallContext context, String source, String target, String filter_file, String reference) {
        copy(context, new File(source), new File(target), new File(filter_file), new File(reference));
    }

    static protected Pattern sr;
    static protected Pattern tr;
    static protected Pattern fir;
    static protected Pattern fer;
    static protected Pattern dir;
    static protected Pattern der;
    static protected Pattern pir;
    static protected Pattern per;

    static protected String tryGetMatch(CallContext context, Pattern pattern, String line) {
        Matcher matcher = pattern.matcher(line);
        if ( ! matcher.find()) { return null; }
        return matcher.group(1);
    }

    static protected String addRegexp(CallContext context, String regexp1, String regexp2) {
        if (regexp1 == null || regexp1.isEmpty()) {
            return "(?:" + regexp2 + ")";
        } else {
            return regexp1 + "|(?:" + regexp2 + ")";
        }
    }

    static public void copy(CallContext context, File source, File target) {
        copy(context, source, target, ".*", "^CVS|\\.svn|\\.git$", ".*", null);
    }

    static public boolean copy(CallContext context, File source, File target, boolean throw_exception) {
        return copy(context, source, target, ".*", "^CVS|\\.svn|\\.git$", ".*", null, throw_exception);
    }

    static public void copy(CallContext context, File source, File target, File filter_file) {
        copy(context, source, target, filter_file, null);
    }

    static public boolean copy(CallContext context, File source, File target, File filter_file, boolean throw_exception) {
        return copy(context, source, target, filter_file, null, throw_exception);
    }

    static public void copy(CallContext context, File source, File target, File filter_file, File reference) {
        copy(context, source, target, filter_file, reference, true);
    }

    static public boolean copy(CallContext context, File source, File target, File filter_file, File reference, boolean throw_exception) {
        if (fir == null) {
            try {
                sr  = Pattern.compile("^\\s*source\\s*(.*)$");
                tr  = Pattern.compile("^\\s*target\\s*(.*)$");
                fir = Pattern.compile("^\\s*file\\s*~\\s*(.*)$");
                fer = Pattern.compile("^\\s*file\\s*!~\\s*(.*)$");
                dir = Pattern.compile("^\\s*folder\\s*~\\s*(.*)$");
                der = Pattern.compile("^\\s*folder\\s*!~\\s*(.*)$");
                pir = Pattern.compile("^\\s*path\\s*~\\s*(.*)$");
                per = Pattern.compile("^\\s*path\\s*!~\\s*(.*)$");
            } catch (PatternSyntaxException pse) {
                CustomaryContext.create(Context.create(context)).throwAssertionProvedFalse(context, pse, "Syntax error in setup of FileUtilities regular expression");
                throw (ExceptionAssertionProvedFalse) null; // compiler insists
            }
        }
        String source_name = null;
        String target_name = null;
        String folder_include_regexp = null;
        String folder_exclude_regexp = null;
        String file_include_regexp = null;
        String file_exclude_regexp = null;
        String path_include_regexp = null;
        String path_exclude_regexp = null;
        for (String line : readFile(context, filter_file)) {
            if (line == null || line.matches("^\\s*(?:#.*)$")) { continue; }
            String match;
            if ((match = tryGetMatch(context, sr , line)) != null) { source_name = match; continue; }
            if ((match = tryGetMatch(context, tr , line)) != null) { target_name = match; continue; }
            if ((match = tryGetMatch(context, dir, line)) != null) { folder_include_regexp = addRegexp(context, folder_include_regexp, match); continue; }
            if ((match = tryGetMatch(context, der, line)) != null) { folder_exclude_regexp = addRegexp(context, folder_exclude_regexp, match); continue; }
            if ((match = tryGetMatch(context, fir, line)) != null) { file_include_regexp   = addRegexp(context, file_include_regexp  , match); continue; }
            if ((match = tryGetMatch(context, fer, line)) != null) { file_exclude_regexp   = addRegexp(context, file_exclude_regexp  , match); continue; }
            if ((match = tryGetMatch(context, pir, line)) != null) { path_include_regexp   = addRegexp(context, path_include_regexp  , match); continue; }
            if ((match = tryGetMatch(context, per, line)) != null) { path_exclude_regexp   = addRegexp(context, path_exclude_regexp  , match); continue; }
        }
        if (source_name != null && source_name.isEmpty() == false) {
            source = new File(source_name).isAbsolute() || reference == null ? new File(source_name) : new File(reference, source_name);
            System.err.println("********* SOURCE OVERRIDE: " + source.getAbsolutePath());
        }
        if (target_name != null && target_name.isEmpty() == false) {
            target = new File(target_name).isAbsolute() || reference == null ? new File(target_name) : new File(reference, target_name);
            System.err.println("********* TARGET OVERRIDE: " + target.getAbsolutePath());
        }
        return copy(context, source, target, folder_include_regexp, folder_exclude_regexp, file_include_regexp, file_exclude_regexp, path_include_regexp, path_exclude_regexp, throw_exception);
    }

    static public void copy(CallContext context, File source, File target, String folder_include_regexp, String folder_exclude_regexp, String file_include_regexp, String file_exclude_regexp) {
        copy(context, source, target, folder_include_regexp, folder_exclude_regexp, file_include_regexp, file_exclude_regexp, null, null, "");
    }

    static public boolean copy(CallContext context, File source, File target, String folder_include_regexp, String folder_exclude_regexp, String file_include_regexp, String file_exclude_regexp, boolean throw_exception) {
        return copy(context, source, target, folder_include_regexp, folder_exclude_regexp, file_include_regexp, file_exclude_regexp, null, null, "", throw_exception);
    }

    static public void copy(CallContext context, File source, File target, String folder_include_regexp, String folder_exclude_regexp, String file_include_regexp, String file_exclude_regexp, String path_include_regexp, String path_exclude_regexp) {
        copy(context, source, target, folder_include_regexp, folder_exclude_regexp, file_include_regexp, file_exclude_regexp, path_include_regexp, path_exclude_regexp, true);
    }

    static public boolean copy(CallContext context, File source, File target, String folder_include_regexp, String folder_exclude_regexp, String file_include_regexp, String file_exclude_regexp, String path_include_regexp, String path_exclude_regexp, boolean throw_exception) {
        return copy(context, source, target, folder_include_regexp, folder_exclude_regexp, file_include_regexp, file_exclude_regexp, path_include_regexp, path_exclude_regexp, "", throw_exception);
    }

    static protected void copy(CallContext context, File source, File target, String folder_include_regexp, String folder_exclude_regexp, String file_include_regexp, String file_exclude_regexp, String path_include_regexp, String path_exclude_regexp, String current_path) {
        copy(context, source, target, folder_include_regexp, folder_exclude_regexp, file_include_regexp, file_exclude_regexp, path_include_regexp, path_exclude_regexp, current_path, true);
    }

    static protected boolean copy(CallContext context, File source, File target, String folder_include_regexp, String folder_exclude_regexp, String file_include_regexp, String file_exclude_regexp, String path_include_regexp, String path_exclude_regexp, String current_path, boolean throw_exception) {
        try {
            if (source.isDirectory()) {
                String[] entries = source.list();
                if (entries != null) {
                    for (String entry : entries) {
                        File source_entry = new File(source, entry);
                        File target_entry = new File(target, entry);
                        String entry_path = current_path + "/" + entry;
                        if (    (    path_include_regexp == null
                                  || entry_path.matches(path_include_regexp)
                                )
                             && (    path_exclude_regexp == null
                                  || (entry_path.matches(path_exclude_regexp) == false)
                                )
                            ) {
                            if (    source_entry.isFile()
                                 && (    file_include_regexp == null
                                      || entry.matches(file_include_regexp)
                                    )
                                 && (    file_exclude_regexp == null
                                      || (entry.matches(file_exclude_regexp) == false)
                                    )
                                ) {
                                copy(context, source_entry, target_entry, folder_include_regexp, folder_exclude_regexp, file_include_regexp, file_exclude_regexp, path_include_regexp, path_exclude_regexp, entry_path);
                            }
                            if (source_entry.isDirectory()) {
                                if (    (    folder_include_regexp == null
                                          || entry.matches(folder_include_regexp)
                                        )
                                     && (    folder_exclude_regexp == null
                                          || (entry.matches(folder_exclude_regexp) == false)
                                        )
                                    ) {
                                    SystemCommandUtilities.ensureFolderExists(context, target_entry);
                                }
                                copy(context, source_entry, target_entry, folder_include_regexp, folder_exclude_regexp, file_include_regexp, file_exclude_regexp, path_include_regexp, path_exclude_regexp, entry_path);
                            }
                        }
                    }
                }
            }
            if (source.isFile()) {
                SystemCommandUtilities.ensureParentFolderExists(context, target);
                FileChannel sourceChannel      = new FileInputStream(source).getChannel();
                FileChannel destinationChannel = new FileOutputStream(target).getChannel();
                sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
                sourceChannel.close();
                destinationChannel.close();
            }
        } catch (IOException ioe) {
            if (throw_exception) {
                CustomaryContext.create((Context)context).throwEnvironmentError(context, ioe, "Could not copy file");
                throw (ExceptionEnvironmentError) null; // compiler insists
            } else {
                return false;
            }
        }
        return true;
    }

    static public void copy(CallContext context, File source, OutputStream os) {
        try {
            copy(context, new FileInputStream(source), os);
        } catch (IOException ioe) {
            CustomaryContext.create((Context)context).throwEnvironmentError(context, ioe, "Could not copy file to stream");
            throw (ExceptionEnvironmentError) null; // compiler insists
        }
    }

    static public void copy(CallContext context, InputStream is, File target) {
        try {
            copy(context, is, new FileOutputStream(target));
        } catch (IOException ioe) {
            CustomaryContext.create((Context)context).throwEnvironmentError(context, ioe, "Could not copy file to stream");
            throw (ExceptionEnvironmentError) null; // compiler insists
        }
    }

    static public void copy(CallContext context, InputStream is, OutputStream os) {
        try {
            byte[] buf = new byte[4096];
            int bread;
            while ((bread = is.read(buf, 0, 4096)) != -1) {
                os.write(buf, 0, bread);
            }
            is.close();
        } catch (IOException ioe) {
            CustomaryContext.create((Context)context).throwEnvironmentError(context, ioe, "Could not copy stream");
            throw (ExceptionEnvironmentError) null; // compiler insists
        }
    }

    static public void copy(CallContext context, InputStream is, Appendable oa) {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            CharBuffer cb = CharBuffer.allocate(4096);
            int cread;
            while ((cread = isr.read(cb)) != -1) {
                cb.flip();
                oa.append(cb);
                cb.clear();
            }
            isr.close();
            is.close();
        } catch (IOException ioe) {
            CustomaryContext.create((Context)context).throwEnvironmentError(context, ioe, "Could not copy stream");
            throw (ExceptionEnvironmentError) null; // compiler insists
        }
    }

    static public boolean move(CallContext context, String source, String target, boolean force) {
        Path sp = FileSystems.getDefault().getPath(source);
        Path tp = FileSystems.getDefault().getPath(target);
        try {
            if (force) {
                Files.move(sp, tp, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.move(sp, tp);
            }
        } catch (IOException ioe) {
            return false;
        }
        return true;
    }

    static public void remove(CallContext context, String source) {
        remove(context, new File(source), null, null, null, null);
    }

    static public void remove(CallContext context, File source) {
        remove(context, source, null, null, null, null);
    }

    static public void remove(CallContext context, File source, String folder_include_regexp, String folder_exclude_regexp, String file_include_regexp, String file_exclude_regexp) {
        if (source.isDirectory()) {
            String[] entries = source.list();
            if (entries != null) {
                for (String entry : entries) {
                    File source_entry = new File(source, entry);
                    if (    source_entry.isDirectory()
                            && (    folder_include_regexp == null
                                    || entry.matches(folder_include_regexp)
                                )
                            && (    folder_exclude_regexp == null
                                    || (entry.matches(folder_exclude_regexp) == false)
                                )
                        ) {
                        remove(context, source_entry, folder_include_regexp, folder_exclude_regexp, file_include_regexp, file_exclude_regexp);
                    } else
                    if (    // source_entry.isFile() <-- not true for symlinks, but there's no isSymLink(...)
                               (    file_include_regexp == null
                                    || entry.matches(file_include_regexp)
                                )
                            && (    file_exclude_regexp == null
                                    || (entry.matches(file_exclude_regexp) == false)
                                )
                        ) {
                        if ( ! source_entry.delete()) {
                            CustomaryContext.create((Context)context).throwEnvironmentError(context, "Could not remove file '%(file)'", "file", source_entry.getPath());
                            throw (ExceptionEnvironmentError) null; // compiler insists
                        }
                    }
                }
            }
            if ( ! source.delete()) {
                CustomaryContext.create((Context)context).throwEnvironmentError(context, "Could not remove folder '%(file)'", "file", source.getPath());
                throw (ExceptionEnvironmentError) null; // compiler insists
            }
        }
        if (source.isFile()) {
            if ( ! source.delete()) {
                CustomaryContext.create((Context)context).throwEnvironmentError(context, "Could not remove file '%(file)'", "file", source.getPath());
                throw (ExceptionEnvironmentError) null; // compiler insists
            }
        }
    }

    static public void createSymbolicLink(CallContext context, String symlink, String target) {
        try {
            java.lang.Runtime.getRuntime().exec("/bin/ln -s -T " + target + " " + symlink);
            // java 1.7
            // java.nio.file.Files.createSymbolicLink(symlink<, target, FileAttribute<?>);
        } catch (IOException ioe) {
            CustomaryContext.create((Context)context).throwPreConditionViolation(context, ioe, "cannot create symbolic link");
            throw (ExceptionPreConditionViolation) null; // compiler insists
        }
    }

    static public void createNamedPipe(CallContext context, String name) {
        try {
            java.lang.Runtime.getRuntime().exec("/usr/bin/mkfifo " + name);
        } catch (IOException ioe) {
            CustomaryContext.create((Context)context).throwPreConditionViolation(context, ioe, "could create named pipe '%(name)'", "name", name);
            throw (ExceptionPreConditionViolation) null; // compiler insists
        }
    }

    static public void modifyPermissions(CallContext context, String source, String owner, String permissions) {
        modifyPermissions(context, new File(source), owner, permissions);
    }

    static public void modifyPermissions(CallContext context, File source, String owner, String permissions) {
        if (source.isFile()) {
            if (permissions != null && permissions.isEmpty() == false) {
                try {
                    java.lang.Runtime.getRuntime().exec("chmod " + permissions + " " + source.getPath());
                } catch (IOException ioe) {
                    CustomaryContext.create((Context)context).throwPreConditionViolation(context, ioe, "cannot modify permissions of '%(file)", "file", source.getPath());
                    throw (ExceptionPreConditionViolation) null; // compiler insists
                }
            }
            if (owner != null && owner.isEmpty() == false) {
                try {
                    java.lang.Runtime.getRuntime().exec("chown " + owner + " " + source.getPath());
                } catch (IOException ioe) {
                    CustomaryContext.create((Context)context).throwPreConditionViolation(context, ioe, "cannot modify owner of '%(file)", "file", source.getPath());
                    throw (ExceptionPreConditionViolation) null; // compiler insists
                }
            }
        } else if (source.isDirectory()) {
            String[] entries = source.list();
            if (entries != null) {
                for (String entry : entries) {
                    File source_entry = new File(source, entry);
                    modifyPermissions(context, source_entry, permissions, owner);
                }
            }
        }
    }

    static public Properties loadPropertiesFrom(CallContext context, String property_file) {
        return loadPropertiesFrom(context, new File(property_file));
    }

    static public Properties loadPropertiesFrom(CallContext context, File property_file) {
        Properties properties = new Properties();
        if (property_file != null && property_file.exists()) {
            InputStream in;
            try {
                in = new FileInputStream(property_file);
            } catch (FileNotFoundException fnfe) {
                CustomaryContext.create((Context)context).throwPreConditionViolation(context, fnfe, "Cannot find file '%(filename)'", "filename", property_file.getPath());
                throw (ExceptionPreConditionViolation) null; // compiler insists
            }
            try {
                properties.load(in);
                in.close();
            } catch (IOException ioe) {
                CustomaryContext.create((Context)context).throwPreConditionViolation(context, ioe, "Cannot read from '%(filename)'", "filename", property_file.getPath());
                throw (ExceptionPreConditionViolation) null; // compiler insists
            }
        }
        return properties;
    }

    static public String getRelativePathBetween(CallContext context, File ancestor, File decestor) {
        if (ancestor == null) {
            CustomaryContext.create((Context)context).throwPreConditionViolation(context, "Cannot get relative path if ancestor is 'null'");
            throw (ExceptionPreConditionViolation) null; // compiler insists
        }
        if (decestor == null) {
            CustomaryContext.create((Context)context).throwPreConditionViolation(context, "Cannot get relative path if decestor is 'null'");
            throw (ExceptionPreConditionViolation) null; // compiler insists
        }

        String ancestor_path = ancestor.getAbsolutePath().replaceFirst("([^/])/+$","$1");
        String decestor_path = decestor.getAbsolutePath().replaceFirst("([^/])/+$","$1");

        if (decestor_path.startsWith(ancestor_path) == false) {
            CustomaryContext.create((Context)context).throwPreConditionViolation(context, "Cannot get relative path, decestor '%(decestor)' does not start with ancestor '%(ancestor)'", "decestor", decestor, "ancestor", ancestor);
            throw (ExceptionPreConditionViolation) null; // compiler insists
        }

        String relative = decestor_path.substring(ancestor_path.length(), decestor_path.length());
        relative = relative.replaceFirst("^/+","");

        return relative;
    }

    static public String getDigest(CallContext context, InputStream is) {
        MessageDigest md;
        String algorithm = "MD5"; // "SHA-512" "SHA-256" "SHA1"
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException nsae) {
            CustomaryContext.create(Context.create(context)).throwConfigurationError(context, "MessageDigest algorithm '%(algorithm)' not available", "algorithm", algorithm);
            throw (ExceptionConfigurationError) null; // compiler insists
        }

        try {
            byte[] data = new byte[4096];
            int cread;
            while ((cread = is.read(data)) != -1) {
                md.update(data);
            }
            is.close();
        } catch (IOException ioe) {
            CustomaryContext.create((Context)context).throwEnvironmentError(context, ioe, "Could not read from stream (calculating digest)");
            throw (ExceptionEnvironmentError) null; // compiler insists
        }

        byte[] bytes = md.digest();

        String s = StringUtilities.convertToHexString(context, bytes);

        return s;
   }
}
