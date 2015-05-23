package com.thoughtworks.pli.remotepair.idea

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.protocol.ParseEvent
import com.thoughtworks.pli.intellij.remotepair.utils.{IsSubPath, Md5, NewUuid}
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.client._
import com.thoughtworks.pli.remotepair.core.editor_event_handlers._
import com.thoughtworks.pli.remotepair.core.server_event_handlers._
import com.thoughtworks.pli.remotepair.core.server_event_handlers.document.{HandleChangeContentConfirmation, HandleCreateDocumentConfirmation, HandleCreateServerDocumentRequest, HandleDocumentSnapshotEvent}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.editors._
import com.thoughtworks.pli.remotepair.core.server_event_handlers.files._
import com.thoughtworks.pli.remotepair.core.server_event_handlers.login.{HandleClientInfoResponse, HandleCreatedProjectEvent, HandleJoinedToProjectEvent, HandleServerStatusResponse}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.syncfiles.{HandleSyncFileEvent, HandleSyncFilesForAll, HandleSyncFilesRequest, PublishSyncFilesRequest}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.watching.{HandleGetWatchingFilesFromPair, HandleMasterWatchingFiles, HandleWatchFilesChangedEvent}
import com.thoughtworks.pli.remotepair.core.tree.{CreateFileTree, FileTreeNodeData}
import com.thoughtworks.pli.remotepair.idea.actions.StartServer
import com.thoughtworks.pli.remotepair.idea.dialogs._
import com.thoughtworks.pli.remotepair.idea.dialogs.list.{GetListItems, InitListItems}
import com.thoughtworks.pli.remotepair.idea.dialogs.utils.{GetSelectedFromFileTree, InitFileTree}
import com.thoughtworks.pli.remotepair.idea.editor._
import com.thoughtworks.pli.remotepair.idea.file._
import com.thoughtworks.pli.remotepair.idea.idea._
import com.thoughtworks.pli.remotepair.idea.listeners._
import com.thoughtworks.pli.remotepair.idea.models._
import com.thoughtworks.pli.remotepair.idea.project._
import com.thoughtworks.pli.remotepair.idea.settings._
import com.thoughtworks.pli.remotepair.idea.statusbar.PairStatusWidget
import com.thoughtworks.pli.remotepair.idea.utils._

trait UtilsModule {
  lazy val newUuid = new NewUuid
  lazy val ideaLogger = Logger.getInstance(this.getClass)

  lazy val md5 = new Md5
  lazy val isSubPath = new IsSubPath
  lazy val getLocalHostName = new GetLocalHostName
  lazy val localIp = new GetLocalIp
  lazy val getIdeaProperties = new GetIdeaProperties
  lazy val clientNameInGlobalStorage = new ClientNameInGlobalStorage(getIdeaProperties, getLocalHostName)
  lazy val serverPortInGlobalStorage = new ServerPortInGlobalStorage(getIdeaProperties)
  lazy val fileTreeNodeDataFactory: FileTreeNodeData.Factory = (file) => new FileTreeNodeData(file)
  lazy val newHighlights = new NewHighlights
  lazy val removeOldHighlighters = new RemoveOldHighlighters
  lazy val getDocumentContent = new GetDocumentContent
  lazy val initListItems = new InitListItems
  lazy val getListItems = new GetListItems
  lazy val removeSelectedItemsFromList = new RemoveSelectedItemsFromList
  lazy val synchronized = new Synchronized

}

trait Module extends UtilsModule {
  def currentIdeaProject: Project

  lazy val currentProject: IdeaProjectImpl = new IdeaProjectImpl(currentIdeaProject)(ideaFactories)

  lazy val runtimeAssertions = new RuntimeAssertions(isSubPath, logger)

  lazy val myClient = new MyClient(currentProject, isSubPath, createFileTree)
  lazy val logger = new PluginLogger(ideaLogger, myClient)
  lazy val clientVersionedDocuments = new ClientVersionedDocuments(currentProject, clientVersionedDocumentFactory)
  lazy val pairEventListeners = new PairEventListeners(currentProject, ideaPlatform)

  lazy val getMessageBus = new GetMessageBus(currentProject)
  lazy val notifyChanges = new NotifyChanges(getMessageBus)
  lazy val getCurrentProjectProperties = new GetCurrentProjectProperties(currentProject)
  lazy val targetProjectNameInProjectStorage = new ProjectNameInProjectStorage(getCurrentProjectProperties)
  lazy val serverPortInProjectStorage = new ServerPortInProjectStorage(getCurrentProjectProperties)
  lazy val serverHostInProjectStorage = new ServerHostInProjectStorage(getCurrentProjectProperties)
  lazy val showErrorDialog = new ShowErrorDialog(currentProject)
  lazy val showMessageDialog = new ShowMessageDialog(currentProject)
  lazy val myPlatform = new IdeaPlatformImpl(currentProject)
  lazy val getLocalIp = new GetLocalIp()
  lazy val startServer = new StartServer(currentProject, myPlatform, getLocalIp, serverPortInGlobalStorage, logger, myClient, showMessageDialog, showErrorDialog: ShowErrorDialog)
  lazy val createFileTree = new CreateFileTree(fileTreeNodeDataFactory)
  lazy val isInPathList = new IsInPathList(currentProject)
  lazy val publishSyncFilesRequest = new PublishSyncFilesRequest(myClient)
  lazy val getFileEditorManager = new GetFileEditorManager(currentProject)
  lazy val tabEventsLocksInProject = new TabEventsLocksInProject(currentProject, getCurrentTimeMillis)
  lazy val getCurrentTimeMillis = new GetCurrentTimeMillis()
  lazy val handleOpenTabEvent = new HandleOpenTabEvent(currentProject, tabEventsLocksInProject, getCurrentTimeMillis, ideaPlatform)
  lazy val handleCloseTabEvent = new HandleCloseTabEvent(currentProject, ideaPlatform)
  lazy val publishCreateDocumentEvent = new PublishCreateDocumentEvent(myClient)
  lazy val clientVersionedDocumentFactory: ClientVersionedDocument.Factory = new ClientVersionedDocument(_)(logger, myClient, newUuid, getCurrentTimeMillis)
  lazy val getEditorsOfPath = new GetEditorsOfPath(currentProject, getFileEditorManager)
  lazy val getTextEditorsOfPath = new GetTextEditorsOfPath(getEditorsOfPath)
  lazy val writeToProjectFile = new WriteToProjectFile(currentProject, getTextEditorsOfPath)
  lazy val highlightNewContent = new HighlightNewContent(getTextEditorsOfPath, newHighlights, removeOldHighlighters, getDocumentContent)
  lazy val handleChangeContentConfirmation = new HandleChangeContentConfirmation(currentProject, myClient, ideaPlatform, logger, clientVersionedDocuments, writeToProjectFile, highlightNewContent, synchronized)
  lazy val convertEditorOffsetToPoint = new ConvertEditorOffsetToPoint()
  lazy val scrollToCaretInEditor = new ScrollToCaretInEditor(convertEditorOffsetToPoint)
  lazy val drawCaretInEditor = new DrawCaretInEditor(convertEditorOffsetToPoint)
  lazy val handleMoveCaretEvent = new HandleMoveCaretEvent(currentProject, ideaPlatform, myClient)
  lazy val handleCreateServerDocumentRequest = new HandleCreateServerDocumentRequest(currentProject, ideaPlatform, myClient)
  lazy val getDocumentLength = new GetDocumentLength(getDocumentContent)
  lazy val highlightPairSelection = new HighlightPairSelection(currentProject, ideaPlatform, myClient, logger)
  lazy val handleSyncFilesRequest = new HandleSyncFilesRequest(myClient)
  lazy val handleMasterWatchingFiles = new HandleMasterWatchingFiles(myClient, ideaPlatform, logger)
  lazy val handleCreateDocumentConfirmation = new HandleCreateDocumentConfirmation(writeToProjectFile, ideaPlatform, clientVersionedDocuments)
  lazy val handleGetPairableFilesFromPair = new HandleGetWatchingFilesFromPair(myClient)
  lazy val handleServerStatusResponse = new HandleServerStatusResponse(myClient)
  lazy val handleJoinedToProjectEvent = new HandleJoinedToProjectEvent(currentProject, ideaPlatform)
  lazy val handleSyncFilesForAll = new HandleSyncFilesForAll(ideaPlatform, publishSyncFilesRequest)
  lazy val handleSyncFileEvent = new HandleSyncFileEvent(writeToProjectFile, ideaPlatform)
  lazy val handleCreateDirEvent = new HandleCreateDirEvent(currentProject, ideaPlatform, logger)
  lazy val handleDeleteFileEvent = new HandleDeleteFileEvent(currentProject, ideaPlatform, logger)
  lazy val handleClientInfoResponse = new HandleClientInfoResponse(myClient)
  lazy val handleDeleteDirEvent = new HandleDeleteDirEvent(currentProject, ideaPlatform, logger)
  lazy val handleCreateFileEvent = new HandleCreateFileEvent(ideaPlatform, writeToProjectFile, logger)
  lazy val showServerError = new ShowServerError(showErrorDialog)
  lazy val handleCreatedProjectEvent = new HandleCreatedProjectEvent()
  lazy val handleDocumentSnapshotEvent = new HandleDocumentSnapshotEvent(currentProject, clientVersionedDocuments, logger, writeToProjectFile, ideaPlatform)
  lazy val handleRenameDirEvent = new HandleRenameDirEvent(currentProject, ideaPlatform, logger)
  lazy val handleRenameFileEvent = new HandleRenameFileEvent(currentProject, ideaPlatform, logger)
  lazy val handleMoveDirEvent = new HandleMoveDirEvent(currentProject, ideaPlatform, logger)
  lazy val handleMoveFileEvent = new HandleMoveFileEvent(currentProject, ideaPlatform, logger)
  lazy val handleWatchFilesChangedEvent = new HandleWatchFilesChangedEvent(myClient, syncFilesForSlaveDialogFactory)
  lazy val handleEvent = new HandleEvent(handleOpenTabEvent, handleCloseTabEvent, myPlatform, myClient, handleChangeContentConfirmation, handleMoveCaretEvent, highlightPairSelection, handleSyncFilesRequest, handleMasterWatchingFiles, handleCreateServerDocumentRequest, handleCreateDocumentConfirmation, handleGetPairableFilesFromPair, handleJoinedToProjectEvent, handleCreatedProjectEvent, handleServerStatusResponse, handleClientInfoResponse, handleSyncFilesForAll, handleSyncFileEvent, handleCreateDirEvent, handleDeleteFileEvent, handleDeleteDirEvent, handleCreateFileEvent, handleRenameDirEvent, handleRenameFileEvent, handleMoveDirEvent, handleMoveFileEvent, handleDocumentSnapshotEvent, handleWatchFilesChangedEvent, logger, md5: Md5)
  lazy val connectionFactory: Connection.Factory = (channelHandlerContext) => new Connection(channelHandlerContext)(logger)
  lazy val myChannelHandlerFactory: MyChannelHandler.Factory = () => new MyChannelHandler(myClient, handleEvent, pairEventListeners, connectionFactory: Connection.Factory, logger: PluginLogger)
  lazy val getSelectedFromFileTree = new GetSelectedFromFileTree
  lazy val resetTreeWithExpandedPathKept = new ResetTreeWithExpandedPathKept
  lazy val initFileTree = new InitFileTree(currentProject, resetTreeWithExpandedPathKept, createFileTree)
  lazy val removeDuplicatePaths = new RemoveDuplicatePaths(isSubPath)
  lazy val getProjectWindow = new GetProjectWindow(currentProject)
  lazy val watchFilesDialogFactory: WatchFilesDialog.Factory = (extraOnCloseHandler) => new WatchFilesDialog(extraOnCloseHandler)(myPlatform, myClient, pairEventListeners, isSubPath, getSelectedFromFileTree, getListItems, removeSelectedItemsFromList, removeDuplicatePaths, initListItems, initFileTree, getProjectWindow, showErrorDialog, isInPathList: IsInPathList)
  lazy val parseEvent = new ParseEvent
  lazy val clientFactory: NettyClient.Factory = (serverAddress) => new NettyClient(serverAddress)(parseEvent, logger)
  lazy val copyToClipboard = new CopyToClipboard()
  lazy val projectUrlInProjectStorage = new ProjectUrlInProjectStorage(getCurrentProjectProperties)
  lazy val copyProjectUrlDialogFactory: CopyProjectUrlDialog.Factory = () => new CopyProjectUrlDialog(ideaPlatform, getProjectWindow, pairEventListeners, copyToClipboard, projectUrlInProjectStorage, logger)
  lazy val clientNameInCreationInProjectStorage = new ClientNameInCreationInProjectStorage(getCurrentProjectProperties)
  lazy val clientNameInJoinInProjectStorage = new ClientNameInJoinInProjectStorage(getCurrentProjectProperties)
  lazy val connectServerDialogFactory: ConnectServerDialog.Factory = () => new ConnectServerDialog(myPlatform, pairEventListeners, myChannelHandlerFactory: MyChannelHandler.Factory, clientFactory: NettyClient.Factory, serverHostInProjectStorage, serverPortInProjectStorage, clientNameInCreationInProjectStorage, clientNameInJoinInProjectStorage, getProjectWindow, newUuid, watchFilesDialogFactory: WatchFilesDialog.Factory, copyProjectUrlDialogFactory: CopyProjectUrlDialog.Factory, projectUrlInProjectStorage, syncFilesForSlaveDialogFactory: SyncFilesForSlaveDialog.Factory, myClient: MyClient)
  lazy val getCaretOffset = new GetCaretOffset
  lazy val handleIdeaFileEvent = new HandleIdeaFileEvent(currentProject, myPlatform, myClient, logger, clientVersionedDocuments, writeToProjectFile: WriteToProjectFile)
  lazy val handleCaretChangeEvent = new HandleCaretChangeEvent(myClient, logger, getDocumentContent, getCaretOffset: GetCaretOffset)
  lazy val handleDocumentChangeEvent = new HandleDocumentChangeEvent(myPlatform, myClient, publishCreateDocumentEvent, newUuid, logger, clientVersionedDocuments: ClientVersionedDocuments)
  lazy val handleSelectionEvent = new HandleSelectionEvent(myClient, logger, getSelectionEventInfo)
  lazy val handleFileTabEvents = new HandleFileTabEvents(publishCreateDocumentEvent, logger, myClient, tabEventsLocksInProject)
  lazy val handleIdeaEvent = new HandleIdeaEvent(handleCaretChangeEvent, handleDocumentChangeEvent, handleFileTabEvents, handleIdeaFileEvent, handleSelectionEvent)
  lazy val projectCaretListenerFactory = new ProjectCaretListenerFactory(logger, handleIdeaEvent, getCaretOffset, ideaFactories)
  lazy val getSelectionEventInfo = new GetSelectionEventInfo()
  lazy val projectSelectionListenerFactory = new ProjectSelectionListenerFactory(logger, handleIdeaEvent, getSelectionEventInfo, ideaFactories)
  lazy val projectDocumentListenerFactory = new ProjectDocumentListenerFactory(logger, handleIdeaEvent, ideaFactories)
  lazy val myFileEditorManagerFactory: MyFileEditorManager.Factory = () => new MyFileEditorManager(handleIdeaEvent, logger, projectDocumentListenerFactory, projectCaretListenerFactory, projectSelectionListenerFactory, ideaFactories)
  lazy val myVirtualFileAdapterFactory: MyVirtualFileAdapter.Factory = () => new MyVirtualFileAdapter(currentProject, handleIdeaEvent, myPlatform, myClient, logger, clientVersionedDocuments, writeToProjectFile, isSubPath, ideaFactories: IdeaFactories)
  lazy val syncFilesForSlaveDialogFactory: SyncFilesForSlaveDialog.Factory = () => new SyncFilesForSlaveDialog(myClient, watchFilesDialogFactory, ideaPlatform, pairEventListeners, getProjectWindow)
  lazy val syncFilesForMasterDialogFactory: SyncFilesForMasterDialog.Factory = () => new SyncFilesForMasterDialog(ideaPlatform, myClient, watchFilesDialogFactory, pairEventListeners, getProjectWindow)
  lazy val copyProjectUrlToClipboard = new CopyProjectUrlToClipboard(projectUrlInProjectStorage, copyToClipboard)
  lazy val statusWidgetPopups = new StatusWidgetPopups(myClient, ideaPlatform, localIp, syncFilesForMasterDialogFactory, syncFilesForSlaveDialogFactory, showErrorDialog, copyProjectUrlToClipboard)
  lazy val createMessageConnection = new CreateMessageConnection(getMessageBus, currentProject)
  lazy val pairStatusWidgetFactory: PairStatusWidget.Factory = () => new PairStatusWidget(statusWidgetPopups, logger, myClient, createMessageConnection)
  lazy val getStatusBar = new GetStatusBar(currentProject)
  lazy val ideaFactories = new IdeaFactories(currentProject, md5, newHighlights, scrollToCaretInEditor, removeOldHighlighters, drawCaretInEditor)
  lazy val ideaPlatform = ideaFactories.platform
}
