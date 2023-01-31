package jiren.view

import com.vaadin.flow.component.*
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.messages.MessageInput
import com.vaadin.flow.component.messages.MessageList
import com.vaadin.flow.component.messages.MessageListItem
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.spring.annotation.SpringComponent
import jiren.controller.UserController
import jiren.controller.twilio.MessageController
import jiren.data.entity.twilio.Instance
import jiren.data.entity.twilio.Message
import jiren.data.enum.MessageTypes
import jiren.security.SecurityService
import jiren.service.apis.twilio.TwilioClient

@SpringComponent
@Tag("refresher")
class ChatComponent(
    val messageController: MessageController,
    val userController: UserController,
    val twilioClient: TwilioClient
) : Component() {

    var chatList: MutableList<ChatInfo> = ArrayList()
    var livechat: HorizontalLayout = HorizontalLayout()

    @ClientCallable
    private fun updateChat() {
        chatList.forEach { chat ->
            val messageList = messageController.listMessages(chat.instance).toMutableList()
            if (messageList.size > chat.messages.size) {
                chat.messages.forEach { msg -> messageList.removeIf { it.id == msg.id } }
                chat.messages.addAll(messageList)
                chat.btn.addThemeVariants(ButtonVariant.LUMO_ERROR)
                val messageItens: MutableList<MessageListItem> = ArrayList()
                messageItens.addAll(chat.view.items)
                messageItens.addAll(
                    messageList.map {
                        val name = if (it.type == MessageTypes.INCOMING) {
                            it.profileName
                        } else {
                            it.instance?.user?.name
                        }
                        MessageListItem(it.body, it.createdAt?.toInstant(), name)
                    })
                chat.view.setItems(messageItens)
                if(messageList.isNotEmpty() && !chat.dialog.isOpened) {
                    Notification.show("Você possui novas mensagens - ${chat.instance.contactName}", 5000, Notification.Position.TOP_END)
                    play()
                }
                UI.getCurrent().push()
            }
        }
        val newInstances = userController.getOpenInstances(
            userController.findByUsername(
                SecurityService().authenticatedUser ?: ""
            )
        ).toMutableList()
        val instances = chatList.map { it.instance }.toMutableList()
        instances.forEach { ins -> newInstances.removeIf { it.id == ins.id } }
        if (newInstances.isNotEmpty()) createChat(newInstances)
    }

    fun createChat(instances: List<Instance>) {
        instances.forEach { instance ->
            val chatDialog = Dialog()
            chatDialog.headerTitle = "${instance.contactName} - ${instance.contact}"
            chatDialog.isModal = false
            chatDialog.isDraggable = true
            chatDialog.isResizable = true
            chatDialog.setId("${instance.id}")

            val dialogBtn = Button(instance.contactName, Icon("chat"))
            dialogBtn.addClickListener {
                chatDialog.isOpened = !chatDialog.isOpened
                dialogBtn.removeThemeVariants(ButtonVariant.LUMO_ERROR)
            }
            dialogBtn.addThemeVariants(ButtonVariant.LUMO_ERROR)
            livechat.add(dialogBtn, chatDialog)

            val instanceBtn = Button("Encerrar", Icon("close")) {
                userController.closeInstance(instance)
                chatDialog.close()
                livechat.remove(dialogBtn, chatDialog)
                chatList.removeIf { it.instance.id == instance.id }
                twilioClient.sendMessage(instance, "Sua conversa foi encerrada")
            }
            instanceBtn.addThemeVariants(ButtonVariant.LUMO_SMALL)

            val dialogLayout = VerticalLayout()
            dialogLayout.setHeightFull()
            chatDialog.add(dialogLayout)

            val msgView = MessageList()
            val messageList = messageController.listMessages(instance)
            val messageItens: List<MessageListItem> = messageList.map { message ->
                val name = if (message.type == MessageTypes.INCOMING) {
                    message.profileName
                } else {
                    instance.user?.name
                }
                MessageListItem(message.body, message.createdAt?.toInstant(), name)
            }

            msgView.setSizeFull()
            msgView.setItems(messageItens)

            val ch = ChatInfo(instance, chatDialog, msgView, messageList.toMutableList(), dialogBtn)
            chatList.add(ch)

            val inputArea = MessageInput()
            inputArea.addSubmitListener { event ->
                val sentMsg = twilioClient.sendMessage(instance, event.value)
                ch.messages.add(sentMsg)
                msgView.setItems(ch.messages.map { message ->
                    MessageListItem(message.body, message.createdAt?.toInstant(), "${instance.user?.name}")
                })
            }
            dialogLayout.add(instanceBtn, msgView, inputArea)
            Notification.show("Você possui novas mensagens - ${instance.contactName}", 5000, Notification.Position.TOP_END)
            play()
        }
        UI.getCurrent().push()
    }

    fun play() {
        UI.getCurrent().page.executeJs("var audio = new Audio('https://res.cloudinary.com/dxfq3iotg/video/upload/v1557233563/warning.mp3');\naudio.play();")
    }

    class ChatInfo(val instance: Instance, val dialog: Dialog, val view: MessageList, val messages: MutableList<Message>, val btn: Button)

}