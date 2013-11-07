package rb;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diff.impl.patch.FilePatch;
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.log4j.xml.DOMConfigurator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.svn.SvnUtil;
import org.jetbrains.idea.svn.SvnVcs;
import org.tmatesoft.svn.core.SVNURL;

import java.io.File;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Gong Zeng
 * Date: 5/13/11
 * Time: 4:17 PM
 */
public class PostReviewAction extends AnAction {

    private Long start = System.currentTimeMillis();

    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = event.getData(PlatformDataKeys.PROJECT);
        final SvnVcs svnVcs = SvnVcs.getInstance(project);
        final VirtualFile[] vFiles = event.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
        if (vFiles == null || vFiles.length == 0) {
            FileIOUtils.fileWrite("No file to be review!!", "LOG.txt", start);
            Messages.showMessageDialog("No file to be review", "Alert", null);
            return;
        }
        if (!ProjectLevelVcsManager.getInstance(project).checkAllFilesAreUnder(svnVcs, vFiles)) {
            setActionEnable(event, true);
            return;
        }

        final ChangeListManager changeListManager = ChangeListManager.getInstance(project);
        changeListManager.invokeAfterUpdate(new Runnable() {
            @Override
            public void run() {
                FileIOUtils.fileWrite("Executing...", "LOG.txt", start);
                System.out.println("Executing...");
                execute(project, svnVcs, vFiles, changeListManager);
            }
        }, InvokeAfterUpdateMode.SYNCHRONOUS_CANCELLABLE, "Refresh VCS", ModalityState.current());
    }

    @Override
    public void update(AnActionEvent event) {
        DOMConfigurator.configure("log4j.xml");
        final Project project = event.getData(PlatformDataKeys.PROJECT);

        final VirtualFile[] vFiles = event.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
        if (vFiles == null || vFiles.length == 0) {
            setActionEnable(event, false);
            return;
        }
        final ChangeListManager changeListManager = ChangeListManager.getInstance(project);

        int enableCount = 0;
        for (VirtualFile vf : vFiles) {

            if (vf != null) {
                vf.refresh(false, true);
                Change change = changeListManager.getChange(vf);
                if (change != null) {
                    if (change.getType().equals(Change.Type.NEW)) {
                        enableCount++;
                        continue;
                    }
                    ContentRevision beforeRevision = change.getBeforeRevision();
                    if (beforeRevision != null) {
                        VcsRevisionNumber revisionNumber = beforeRevision.getRevisionNumber();
                        if (!revisionNumber.equals(VcsRevisionNumber.NULL)) {
                            enableCount++;
                        }
                    }
                }
            }
        }
        setActionEnable(event, enableCount == vFiles.length);
    }

    private void setActionEnable(AnActionEvent event, boolean isEnable) {
        event.getPresentation().setEnabled(isEnable);
    }

    private void execute(final Project project, SvnVcs svnVcs, VirtualFile[] vFiles, ChangeListManager changeListManager) {
        List<Change> changes = new ArrayList<Change>();
        String changeMessage = null;
        String localRootDir = null;
        String remoteRootUrl = null;
        String repositoryUrl = null;
        final String patch;
        for (VirtualFile vf : vFiles) {
            if (vf != null) {
                vf.refresh(false, true);
                File workingCopyRoot = SvnUtil.getWorkingCopyRoot(new File(vf.getPath()));
                FileIOUtils.fileWrite("workcopyroot: " + workingCopyRoot, "LOG.txt", start);
                if (localRootDir == null && workingCopyRoot != null) {
                    localRootDir = workingCopyRoot.getPath();
                }
                SVNURL url = SvnUtil.getUrl(svnVcs, workingCopyRoot);
                FileIOUtils.fileWrite("remoteRootUrl: " + url, "LOG.txt", start);
                if (url != null && remoteRootUrl == null) {
                    remoteRootUrl = url.toString();
                }
                SVNURL repositoryRoot = SvnUtil.getRepositoryRoot(svnVcs, workingCopyRoot);
                FileIOUtils.fileWrite("repository: " + repositoryRoot, "LOG.txt", start);
                if (repositoryRoot != null && repositoryUrl == null) {
                    repositoryUrl = repositoryRoot.toString();
                    FileIOUtils.fileWrite("repositoryUrl: " + repositoryUrl, "LOG.txt", start);
                }

                Change change = changeListManager.getChange(vf);
                if (change != null && change.getType().equals(Change.Type.NEW)) {
                    final ContentRevision afterRevision = change.getAfterRevision();
                    change = new Change(null, new ContentRevision() {
                        @Override
                        public String getContent() throws VcsException {
                            return afterRevision.getContent();
                        }

                        @NotNull
                        @Override
                        public FilePath getFile() {
                            return afterRevision.getFile();
                        }

                        @NotNull
                        @Override
                        public VcsRevisionNumber getRevisionNumber() {
                            return new VcsRevisionNumber.Int(0);
                        }
                    }, change.getFileStatus()
                    );
                }
                changes.add(change);
                if (changeMessage == null) {
                    LocalChangeList changeList = changeListManager.getChangeList(vf);
                    if (changeList != null) {
                        changeMessage = changeList.getName();
                    }
                }
            }
        }

        if (localRootDir == null) {
            FileIOUtils.fileWrite("No base path ", "LOG.txt", start);
            Messages.showErrorDialog("No base path", null);
            return;
        }
        if (repositoryUrl == null) {
            FileIOUtils.fileWrite("No repository Url ", "LOG.txt", start);
            Messages.showErrorDialog("No repository Url", null);
            return;
        }
        if (remoteRootUrl == null) {
            FileIOUtils.fileWrite("No remoteRootUrl Url ", "LOG.txt", start);
            Messages.showErrorDialog("No remoteRootUrl Url", null);
            return;
        }
        int i = remoteRootUrl.indexOf(repositoryUrl);
        final String basePathForReviewBoard;
        if (i != -1) {
            basePathForReviewBoard = remoteRootUrl.substring(i + repositoryUrl.length());
        } else {
            basePathForReviewBoard = "";
        }
        try {
            List<FilePatch> filePatches = buildPatch(project, changes, localRootDir, false);
            if (filePatches == null) {
                FileIOUtils.fileWrite("Create diff error ", "LOG.txt", start);
                Messages.showWarningDialog("Create diff error", "Alter");
                return;
            }
            StringWriter w = new StringWriter();
            UnifiedDiffWriter.write(project, filePatches, w, "\r\n", null);
            w.close();
            patch = w.toString();
        } catch (Exception e) {
            FileIOUtils.fileWrite("Svn is still in refresh. Please try again later", "LOG.txt", start);
            Messages.showWarningDialog("Svn is still in refresh. Please try again later.", "Alter");
            return;
        }

        final String finalRepositoryUrl = repositoryUrl;
        final PrePostReviewForm prePostReviewForm = new PrePostReviewForm(project, changeMessage, patch) {

            @Override
            protected void doOKAction() {
                if (!isOKActionEnabled()) {
                    return;
                }
                final ReviewSettings setting = this.getSetting();
                if (setting.getServer() == null || "".equals(setting.getServer())) {
                    FileIOUtils.fileWrite("Please set the review board server address in config panel:  " + project, "LOG.txt", start);
                    Messages.showMessageDialog(project, "Please set the review board server address in config panel", "Info", null);
                    return;
                }
                if (setting.getUsername() == null || "".equals(setting.getUsername())) {
                    FileIOUtils.fileWrite("Please set the review board user name in config panel:  " + project, "LOG.txt", start);
                    Messages.showMessageDialog(project, "Please set the review board user name in config panel", "Info", null);
                    return;
                }
                if (setting.getPassword() == null || "".equals(setting.getPassword())) {
                    FileIOUtils.fileWrite("Please set the view board password in config panel:  " + project, "LOG.txt", start);
                    Messages.showMessageDialog(project, "Please set the view board password in config panel", "Info", null);
                    return;
                }

                setting.setSvnBasePath(basePathForReviewBoard);
                setting.setSvnRoot(finalRepositoryUrl);
                setting.setDiff(patch);
                PerformInBackgroundOption option = new PerformInBackgroundOption() {
                    @Override
                    public boolean shouldStartInBackground() {
                        return false;
                    }

                    @Override
                    public void processSentToBackground() {
                    }
                };
                Task.Backgroundable task = new
                        Task.Backgroundable(project, "running", false, option) {
                            boolean result;

                            @Override
                            public void onSuccess() {
                                if (result) {
                                    String url = setting.getServer() + "/r/" + setting.getReviewId();
                                    int success = Messages.showYesNoDialog("The review url is " + url + "\r\n" +
                                            "Open the url?", "Success", null);
                                    FileIOUtils.fileWrite("open url is:  " + url, "LOG.txt", start);
                                    if (success == 0) {
                                        BrowserUtil.launchBrowser(url);
                                    }

                                } else {
                                    Messages.showErrorDialog("Post review failure", "Error");
                                    FileIOUtils.fileWrite("Post review failure:  ", "LOG.txt", start);
                                }
                            }

                            @Override
                            public void run(@NotNull ProgressIndicator progressIndicator) {
                                progressIndicator.setIndeterminate(true);
                                result = ReviewBoardClient.postReview(setting, progressIndicator);
                            }
                        };
                ProgressManager.getInstance().run(task);
                super.doOKAction();
            }
        };
        prePostReviewForm.show();
    }

    private List<FilePatch> buildPatch(Project project, List<Change> changes, String localRootDir, boolean b) {
        //      List<FilePatch> filePatches = IdeaTextPatchBuilder.buildPatch(project, changes, localRootDir, false);
//    List<FilePatch> filePatches = TextPatchBuilder.buildPatch(changes, localRootDir, false);
        Object result = null;
        try {//invoke the api in 10.x
            Class c = Class.forName("com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder");
            Method buildPatchMethod = c.getMethod("buildPatch", Project.class, Collection.class, String.class, boolean.class);
            result = buildPatchMethod.invoke(null, project, changes, localRootDir, b);
        } catch (ClassNotFoundException e) {
            try {//API in 9.0x
                Class c = Class.forName("com.intellij.openapi.diff.impl.patch.TextPatchBuilder");
                Method buildPatchMethod = c.getMethod("buildPatch", Collection.class, String.class, boolean.class);
                result = buildPatchMethod.invoke(null, changes, localRootDir, b);
            } catch (Exception e1) {
                Messages.showErrorDialog("The current version doesn't support the review", "Not support");
                return null;
            }
        } catch (Exception e) {
            Messages.showErrorDialog("The current version doesn't support the review", "Not support");
        }
        if (result != null && result instanceof List) {
            return (List<FilePatch>) result;
        }
        return null;
    }

    public boolean isDumbAware() {
        return true;
    }
}
