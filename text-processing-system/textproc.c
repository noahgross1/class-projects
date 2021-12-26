/* This is the only file you should update and submit. */

/* Fill in your Name and GNumber in the following two comment fields
 * Name: Noah Gross
 * GNumber: 01093154
 */

#include <sys/wait.h>
#include "textproc.h"
#include "parse.h"
#include "util.h"

/* Constants */
#define DEBUG 1

/*
static const char *textproc_path[] = { "./", "/usr/bin/", NULL };
static const char *instructions[] = { "quit", "help", "list", "new", "open", "write", "close", "print", "active", "pause", "resume", "cancel", "exec", NULL};
*/

typedef struct Buffer {
    int id;
    int pid;
    char *contents;
    int state;
    char *cmd;
    int fd;
} Buffer;

typedef struct Node {
    Buffer data;
    struct Node* next;
} Node;

void insertBuffer(Node **head, Buffer newData);
void printBufferList(Node *head, int activeBufferId);
int createBuffer (Node **head);
int getMaxBufferId(Node *head);
int getBufferCount(Node *head);
Node *findBuffer(Node *head, int bufferId);
void closeBuffer(Node **head, int bufferId, int *activeBufferId);
void openFile(Node **head, char *file, int *activeBufferId);
void printFileContents(Node *head, int bufferId, int activeBufferId);
void writeFile(Node *head, char *file, int activeBufferId, int bufferId);
void execCommand(Node *head, int bufferId, int activeBufferId, char *argv[], char *command);
void sigChildHandler(int sig);
void newCommand(Node **head, int *activeBufferId, char *argv[], char *command);
Node *findBufferByPid(Node *head, int pid);
void cancelProcess(Node *head, int bufferId, int activeBufferId);
void pauseProcess(Node *head, int bufferId, int activeBufferId);
void resumeProcess(Node *head, int bufferId, int activeBufferId);
void sigIntHandler(int sig);
void sigStopHandler(int sig);

Node *head = NULL;
int activeBufferId = 0;

/* The entry of your text processor program */
int main() {
    char cmdline[MAXLINE];        /* Command line */
    char *cmd = NULL;
    Node *buffer;
    struct sigaction sa;
    struct sigaction sa2;


    /* Intial Prompt and Welcome */
    log_help();

    // setup SIGINT handler.
    memset(&sa, 0, sizeof(sa));
    sa.sa_handler = sigIntHandler;
    sigaction(SIGINT, &sa, NULL);

    // setup SIGTSTP handler.
    memset(&sa2, 0, sizeof(sa2));
    sa2.sa_handler = sigStopHandler;
    sigaction(SIGTSTP, &sa2, NULL);

    /* Shell looping here to accept user command and execute */
    while (1) {
        char *argv[MAXARGS];        /* Argument list */
        Instruction inst;           /* Instruction structure: check parse.h */

        /* Print prompt */
        log_prompt();

        /* Read a line */
        // note: fgets will keep the ending '\n'
        if (fgets(cmdline, MAXLINE, stdin) == NULL) {
            if (errno == EINTR) {
                continue;
            }
            exit(-1);
        }

        if (feof(stdin)) {  /* ctrl-d will exit text processor */
          exit(0);
        }

        /* Parse command line */
        if (strlen(cmdline)==1)   /* empty cmd line will be ignored */
          continue;     

        cmdline[strlen(cmdline) - 1] = '\0';        /* remove trailing '\n' */

        cmd = malloc(strlen(cmdline) + 1);
        snprintf(cmd, strlen(cmdline) + 1, "%s", cmdline);

        /* Bail if command is only whitespace */
        if(!is_whitespace(cmd)) {
            initialize_command(&inst, argv);    /* initialize arg lists and instruction */
            parse(cmd, &inst, argv);            /* call provided parse() */

            if (DEBUG) {  /* display parse result, redefine DEBUG to turn it off */
                debug_print_parse(cmd, &inst, argv, "main (after parse)");
	        }

            /* After parsing: your code to continue from here */
            /*================================================*/

            // Check instructions.
            if (strcmp(inst.instruct, "help") == 0) {
                log_help();
            } else if (strcmp(inst.instruct, "quit") == 0) {
                log_quit();
                exit(0);
            } else if (strcmp(inst.instruct, "new") == 0) {
                // activeBufferId = createBuffer(&head);
                newCommand(&head, &activeBufferId, argv, cmd);
            } else if (strcmp(inst.instruct, "list") == 0) {
                printBufferList(head, activeBufferId);
            } else if (strcmp(inst.instruct, "active") == 0) {
                buffer = findBuffer(head, inst.id);
                // Activate specified buffer Id if found.
                if (buffer != NULL) {
                    activeBufferId = inst.id;
                    log_activate(activeBufferId);
                } else {
                    log_buf_id_error(inst.id);
                }
            } else if (strcmp(inst.instruct, "close") == 0) {
                closeBuffer(&head, inst.id, &activeBufferId);
            } else if (strcmp(inst.instruct, "open") == 0) {
                openFile(&head, inst.file, &activeBufferId);
            } else if (strcmp(inst.instruct, "write") == 0) {
                writeFile(head, inst.file, activeBufferId, inst.id);
            } else if (strcmp(inst.instruct, "print") == 0) {
                printFileContents(head, inst.id, activeBufferId);
            } else if (strcmp(inst.instruct, "exec") == 0) {
                execCommand(head, inst.id, activeBufferId, argv, cmd);
            } else if (strcmp(inst.instruct, "cancel") == 0) {
                cancelProcess(head, inst.id, activeBufferId);
            } else if (strcmp(inst.instruct, "pause") == 0) {
                pauseProcess(head, inst.id, activeBufferId);
            } else if (strcmp(inst.instruct, "resume") == 0) {
                resumeProcess(head, inst.id, activeBufferId);
            }
        }

        free_command(&inst, argv);
    }
    return 0;
}

/**
 * Print contents of file.
 * @param head head of buffer list
 * @param bufferId Id of buffer
 * @param activeBufferId Id of active buffer
 */
void printFileContents(Node *head, int bufferId, int activeBufferId) {
    Node *node;

    // Check if bufferId is not specified by user.
    if (bufferId == 0) {
        bufferId = activeBufferId;
    }
    node = findBuffer(head, bufferId);

    if (node == NULL) {
        log_buf_id_error(bufferId);
    } else {
        log_print(bufferId, node->data.contents);
    }
}

/**
 * Write contents from buffer to file.
 * @param head head of buffer list
 * @param file file you want to open and write to
 * @param activeBufferId active buffer id
 * @param bufferId buffer id
 */
void writeFile(Node *head, char *file, int activeBufferId, int bufferId) {
    int fd;
    Node *node;

    // Check if bufferId is not specified by user.
    if (bufferId == 0) {
        bufferId = activeBufferId;
    }
    node = findBuffer(head, bufferId);

    // Check if buffer was not found.
    if (node == NULL) {
        log_buf_id_error(bufferId);
    } else {
        // Create new file if file doesn't exist, otherwise overwrite existing file.
        fd = open(file, O_WRONLY | O_CREAT | O_TRUNC, 0600);
        // Check for error opening file.
        if (fd == -1) {
            log_file_error(LOG_FILE_OPEN_WRITE, file);
        } else {
            // Write contents of buffer into file.
            text_to_fd(node->data.contents, fd);
            // Successfully written.
            log_write(bufferId, file);
            // Close file descriptor.
            close(fd);
        }
    }
}

/**
 * Handles the SIGINT signal.
 * @param sig signal
 */
void sigIntHandler(int sig) {
   Node *node;

   log_ctrl_c();
   if (activeBufferId > 0) {
       node = findBuffer(head, activeBufferId);
       if (node != NULL) {
           if (node->data.state == LOG_STATE_WORKING) {
               kill(node->data.pid, SIGINT);
           }
       }
   }

}

/**
 * Handles SIGTSTP signal.
 * @param sig signal
 */
void sigStopHandler(int sig) {
    Node *node;

    log_ctrl_z();
    if (activeBufferId > 0) {
        node = findBuffer(head, activeBufferId);
        if (node != NULL) {
            if (node->data.state == LOG_STATE_WORKING) {
                kill(node->data.pid, SIGTSTP);
            }
        }
    }
}

/**
 * Handles SIGCHILD event.
 * @param sig signal
 */
void sigChildHandler(int sig) {
    int childPid;
    int status;
    char command[] = "command";
    Node *node;

    // Poll till child process completed.
    do {
        childPid = waitpid(-1, &status, WUNTRACED | WNOHANG | WCONTINUED);
    } while (childPid == 0);

    if (childPid > 0) {
        // Find buffer for PID.
        node = findBufferByPid(head, childPid);
        if (node != NULL) {
            // Check if child process stopped.
            if (WIFSTOPPED(status)) {
                log_cmd_state(childPid, LOG_BACKGROUND, command, LOG_PAUSE);
                node->data.state = LOG_STATE_PAUSED;
              // Check if child process was killed by signal.
            } else if (WIFSIGNALED(status)) {
                log_cmd_state(childPid, LOG_BACKGROUND, command, LOG_CANCEL_SIG);
                node->data.state = LOG_STATE_READY;
              // Check if child process ended normally.
            } else if (WIFEXITED(status)) {
                log_cmd_state(childPid, LOG_BACKGROUND, command, LOG_CANCEL);
                // Reset pid and state.
                node->data.pid = 0;
                node->data.state = LOG_STATE_READY;

                free(node->data.contents);
                // Read data from parent end of pipe.
                node->data.contents = fd_to_text(node->data.fd);

                // Close parent read end of pipe.
                close(node->data.fd);
              // Check if child process continued after a stop.
            } else if (WIFCONTINUED(status)) {
                log_cmd_state(childPid, LOG_BACKGROUND, command, LOG_RESUME);
                node->data.state = LOG_STATE_WORKING;
            }
        }
    }
}

/**
 * Resume process.
 * @param head head of buffer list
 * @param bufferId buffer id
 * @param activeBufferId active buffer id
 */
void resumeProcess(Node *head, int bufferId, int activeBufferId) {
    Node *node;

    // Check if bufferId is not specified by user.
    if (bufferId == 0) {
        bufferId = activeBufferId;
    }
    node = findBuffer(head, bufferId);

    // Check if buffer not found.
    if (node == NULL) {
        log_buf_id_error(bufferId);
    } else {
        // Check if no process is running.
        if (node->data.state == LOG_STATE_READY) {
            log_cmd_state_conflict(bufferId, node->data.state);
        } else {
            log_cmd_signal(LOG_CMD_RESUME, bufferId);
            kill(node->data.pid, SIGCONT);
        }
    }
}

/**
 * Pause process.
 * @param head head of buffer list
 * @param bufferId buffer id
 * @param activeBufferId active buffer id
 */
void pauseProcess(Node *head, int bufferId, int activeBufferId) {
    Node *node;

    // Check if bufferId is not specified by user.
    if (bufferId == 0) {
        bufferId = activeBufferId;
    }
    node = findBuffer(head, bufferId);

    // Check if buffer not found.
    if (node == NULL) {
        log_buf_id_error(bufferId);
    } else {
        // Check if no process is running.
        if (node->data.state == LOG_STATE_READY) {
            log_cmd_state_conflict(bufferId, node->data.state);
        } else {
            node->data.state = LOG_STATE_PAUSED;
            log_cmd_signal(LOG_CMD_PAUSE, bufferId);
            kill(node->data.pid, SIGTSTP);
        }
    }
}

/**
 * Terminate process in buffer.
 * @param head head of buffer list
 * @param bufferId buffer id
 * @param activeBufferId active buffer id
 */
void cancelProcess(Node *head, int bufferId, int activeBufferId) {
    Node *node;

    // Check if bufferId is not specified by user.
    if (bufferId == 0) {
        bufferId = activeBufferId;
    }
    node = findBuffer(head, bufferId);

    // Check if buffer not found.
    if (node == NULL) {
        log_buf_id_error(bufferId);
    } else {
        // Check if no process is running.
        if (node->data.state == LOG_STATE_READY) {
            log_cmd_state_conflict(bufferId, node->data.state);
        } else {
            log_cmd_signal(LOG_CMD_CANCEL, bufferId);
            kill(node->data.pid, SIGINT);
        }
    }
}

/**
 * Create new buffer, execute command, and update buffers content.
 * @param head head of buffer list
 * @param activeBufferId active buffer id
 * @param argv command arguments
 * @param command command
 */
void newCommand(Node **head, int *activeBufferId, char *argv[], char *command) {
    *activeBufferId = createBuffer(head);
    if (strlen(command) > 3) {
        execCommand(*head, 0, *activeBufferId, argv, command);
    }

}

/**
 * Execute a command against a selected or active buffer contents and update contents with result of command.
 * @param head head of buffer list
 * @param bufferId buffer id
 * @param activeBufferId active buffer id
 * @param argv command arguments
 * @param command command
 */
void execCommand(Node *head, int bufferId, int activeBufferId, char *argv[], char *command) {
    Node *node;
    int ret;
    char cmd[] = "./";
    char cmd2[] = "/usr/bin/";
    int fd1[2];
    int fd2[2];
    int status;
    int childPid;
    int type = LOG_BACKGROUND;
    sigset_t mask;
    sigset_t prev;
    struct sigaction sa;

    // Check if bufferId is not specified by user.
    if (bufferId == 0) {
        bufferId = activeBufferId;
        type = LOG_ACTIVE;
    }
    node = findBuffer(head, bufferId);

    // Check if buffer not found.
    if (node == NULL) {
        log_buf_id_error(bufferId);
    } else {
        // Check if buffer in ready state.
        if (node->data.state != LOG_STATE_READY) {
            log_cmd_state_conflict(bufferId, node->data.state);
        } else {
            // Create pipes.
            if (pipe(fd1) == -1) {
                log_command_error(command);
                return;
            }

            if (pipe(fd2) == -1) {
                log_command_error(command);
                return;
            }

            // Initialize mask to empty
            sigemptyset(&mask);
            // Create a mask for SIGINT
            sigaddset(&mask, SIGINT);
            // Block SIGINT;
            sigprocmask(SIG_BLOCK, &mask, &prev);

            // setup SIGCHLD handler
            memset(&sa, 0, sizeof(sa));
            sa.sa_handler = sigChildHandler;
            sigaction(SIGCHLD, &sa, NULL);

            // Fork child process
            int pid = fork();

            // Restore signals.
            sigprocmask(SIG_SETMASK, &prev, NULL);

            // Fork error.
            if (pid == -1) {
                log_command_error(command);
            }
            // Child process.
            else if (pid == 0) {
                // Create process group.
                setpgid(0, 0);

                // Read buffers content from standard input.
                if (dup2(fd1[0], STDIN_FILENO) == -1) {
                    log_command_error(command);
                    return;
                }

                // Close parent end of read pipe.
                close(fd1[1]);

                // Write result of command to standard output.
                if (dup2(fd2[1], STDOUT_FILENO) == -1) {
                    log_command_error(command);
                    return;
                }
                // Close parent end of write pipe.
                close(fd2[0]);

                // Close child end of read pipe.
                close(fd1[0]);
                // Close child end of write pipe.
                close(fd2[1]);

                // Execute command.
                strcat(cmd, argv[0]);
                ret = execv(cmd, argv);
                if (ret == -1) {
                    strcat(cmd2, argv[0]);
                    log_print(bufferId, cmd2);
                    ret = execv(cmd2, argv);
                    if (ret == -1) {
                        perror("error executing command.");
                        log_command_error(command);
                        return;
                    }
                }
            }
            // Parent process.
            else {
                // Starting an active or background buffer.
                log_start(bufferId, pid, type, command);

                // Set pid and state.
                node->data.pid = pid;
                node->data.state = LOG_STATE_WORKING;
                node->data.fd = fd2[0];

                // Close child end of write pipe.
                close(fd1[0]);
                // Close child end of read pipe.
                close(fd2[1]);

                // Write contents of buffer to parent end of pipe (STDOUT).
                text_to_fd(node->data.contents, fd1[1]);
                // Close parent end of write pipe.
                close(fd1[1]);

                if (bufferId == activeBufferId) {
                    // Do until child process is reaped.
                    do {
                        childPid = waitpid(pid, &status, WUNTRACED | WCONTINUED);
                    } while (childPid == 0);

                    // Log status of active and background buffers.
                    if (WIFEXITED(status)) {
                        log_cmd_state(childPid, type, command, LOG_CANCEL);
                        // Reset pid and state.
                        node->data.pid = 0;
                        node->data.state = LOG_STATE_READY;

                        free(node->data.contents);
                        // Read data from parent end of pipe.
                        node->data.contents = fd_to_text(node->data.fd);

                        // Close parent read end of pipe.
                        close(node->data.fd);
                    } else if (WIFSTOPPED(status)) {
                        log_cmd_state(childPid, type, command, LOG_PAUSE);
                        node->data.state = LOG_STATE_PAUSED;
                    } else if (WIFSIGNALED(status)) {
                        log_cmd_state(childPid, type, command, LOG_CANCEL_SIG);
                        node->data.state = LOG_STATE_READY;
                    } else if (WIFCONTINUED(status)) {
                        log_cmd_state(childPid, type, command, LOG_RESUME);
                    }
                }
            }
        }
    }
}

/**
 * Open file and store contents of file in a new buffer.
 * @param head head of buffer list
 * @param file file to open and read
 */
void openFile(Node **head, char *file, int *activeBufferId) {
    int fd;

    // Open file for read only.
    fd = open(file, O_RDONLY);
    if (fd == -1) {
        log_file_error(LOG_FILE_OPEN_READ, file);
        return;
    }

    *activeBufferId =  createBuffer(head);
    Node *node = findBuffer(*head, *activeBufferId);
    if (node != NULL) {
        // Store contents of file in node.
        node->data.contents = fd_to_text(fd);

        if (node->data.contents != NULL) {
            log_read(*activeBufferId, file);
        }
    }
    close(fd);
}

/**
 * Find buffer by pid.
 * @param head head of buffer list
 * @param pid buffer pid to find
 * @return matching buffer or NULL if not found
 */
Node *findBufferByPid(Node *head, int pid) {
    Node *current = head;

    // Iterate until you find pid.
    while (current != NULL) {
        if (current->data.pid == pid) {
            return current;
        }
        current = current->next;
    }
    return NULL;
}

/**
 * Find buffer in list.
 * @param head head of buffer list
 * @param bufferId buffer Id to find
 * @return pointer to found buffer Id
 */
Node *findBuffer(Node *head, int bufferId) {
    Node *current = head;

    // Iterate until you find bufferId.
    while (current != NULL) {
        if (current->data.id == bufferId) {
            return current;
        }
        current = current->next;
    }
    return NULL;
}

/**
 * Delete buffer from list.
 * @param head head of buffer list
 * @param bufferId  buffer Id to find
 * @return pointer to deleted buffer
 */
void closeBuffer(Node **head, int bufferId, int *activeBufferId) {
    Node *current = *head;
    Node *prev = NULL;

    while (current != NULL) {
        // Check if buffer id was found.
        if (current->data.id == bufferId) {
            // Only close buffer if in ready state.
            if (current->data.state == LOG_STATE_READY) {
                if (current->data.contents != NULL) {
                    free(current);
                }
                if (current == *head) {
                    *head = current->next;
                } else {
                    prev->next = current->next;
                }
                // Successful close.
                log_close(bufferId);
                // Update active buffer id.
                if (bufferId == *activeBufferId) {
                    *activeBufferId = getMaxBufferId(*head);
                    log_activate(*activeBufferId);
                }
            } else {
                // Buffer is not in ready state.
                log_close_error(bufferId);
            }
            return;
        }
        prev = current;
        current = current->next;
    }
    // Buffer is invalid.
    log_buf_id_error(bufferId);
}

/**
 * Create buffer.
 * @param head buffer list.
 * @param maxBufferId largest open buffer id.
 */
int createBuffer(Node **head) {
    Buffer buffer;

    // Initialize new buffer to ready state and update id.
    buffer.state = LOG_STATE_READY;
    buffer.contents = calloc(1, sizeof(char));
    buffer.cmd = NULL;
    buffer.pid = 0;
    buffer.id = getMaxBufferId(*head) + 1;

    // Insert new buffer into list.
    insertBuffer(head, buffer);

    // Indicate which buffer ID was created and is active.
    log_open(buffer.id);
    log_activate(buffer.id);

    return buffer.id;
}

/**
 * Insert new buffer to end of list.
 * @param head head of list
 * @param newData new buffer data
 */
void insertBuffer(Node **head, Buffer newData) {
    Node *newNode = (Node *) malloc(sizeof(Node));
    newNode->data = newData;
    newNode->next = NULL;

    // Check if head is empty.
    if (*head == NULL) {
        *head = newNode;
    } else {
        Node *current = *head;

        // Iterate to end of list.
        while (current->next != NULL) {
            current = current->next;
        }

        // Add new node to end of list.
        current->next = newNode;
    }
}

/**
 * Print list of buffers.
 * @param head head of buffer list
 * @param activeBufferId active buffer id
 */
void printBufferList(Node *head, int activeBufferId) {
    int numBuffers = getBufferCount(head);
    log_buf_count(numBuffers);

    if (head != NULL) {
        Node *current = head;
        while (current != NULL) {
            log_buf_details(current->data.id, current->data.state, current->data.pid, current->data.cmd);
            current = current->next;
        }
    }
    log_show_active(activeBufferId);
}

/**
 * Get buffers max id.
 * @param head head of buffer list
 * @return buffers max id
 */
int getMaxBufferId(Node *head) {
    int maxId = 0;

    Node *current = head;
    // Iterate through buffer list.
    while (current != NULL) {
        // Update buffers max id.
        maxId = current->data.id;
        current = current->next;
    }
    return maxId;
}

/**
 * Count number of buffers in list.
 * @param head head of list.
 * @return number of buffers in list.
 */
int getBufferCount(Node *head) {
    int count = 0;

    Node *current = head;
    while (current != NULL) {
        count++;
        current = current->next;
    }

    return count;
}

