package com.reactcode.seed;

import com.reactcode.domain.Difficulty;

import java.util.List;

public final class ChallengeCatalog {

    private ChallengeCatalog() {
    }

    public static List<SeedChallenge> all() {
        return List.of(
                counter(),
                toggleVisibility(),
                characterCounter(),
                controlledInput(),
                showHidePassword(),
                tabs(),
                searchableList(),
                todoList(),
                accordion(),
                modalDialog(),
                formValidation(),
                starRating(),
                debouncedSearch(),
                pagination(),
                sortableTable(),
                shoppingCart(),
                multiStepForm(),
                autocomplete(),
                nestedComments(),
                dragAndDrop()
        );
    }

    private static SeedChallenge challenge(
            String slug,
            String title,
            Difficulty difficulty,
            boolean mvp,
            int order,
            String shortDescription,
            String statement,
            String starter,
            String tests
    ) {
        return new SeedChallenge(slug, title, difficulty, shortDescription, statement, starter, tests, order, mvp);
    }

    private static SeedChallenge counter() {
        return challenge(
                "counter",
                "Counter Component",
                Difficulty.EASY,
                true,
                1,
                "Build a counter that starts at 0 and increments when the user clicks a button.",
                """
                # Counter Component

                Implement a counter that keeps a numeric value in React state.

                ## Requirements
                - Render the current count in an element with `data-testid="count"`.
                - The count must start at **0**.
                - Render a button whose accessible name is **Increment**.
                - Each click increases the count by 1.

                ## Example
                1. The UI shows `0`.
                2. Click Increment → `1`.
                3. Click Increment again → `2`.

                ## Constraints
                - Use client-side React state (`useState`).
                - Do not persist the count.
                - Export a default function component.
                """,
                """
                import React, { useState } from "react";

                export default function Counter() {
                  const [count, setCount] = useState(0);

                  return (
                    <div>
                      <p data-testid="count">{count}</p>
                      <button onClick={() => setCount(count + 1)}>
                        Increment
                      </button>
                    </div>
                  );
                }
                """,
                """
                {"cases":[
                  {"id":"starts-at-zero","name":"Initially displays 0","steps":[{"type":"assertText","testId":"count","equals":"0"}]},
                  {"id":"increment-once","name":"Clicking Increment changes the count to 1","steps":[{"type":"click","text":"Increment"},{"type":"assertText","testId":"count","equals":"1"}]},
                  {"id":"increment-twice","name":"Clicking Increment again changes the count to 2","steps":[{"type":"click","text":"Increment"},{"type":"click","text":"Increment"},{"type":"assertText","testId":"count","equals":"2"}]}
                ]}
                """
        );
    }

    private static SeedChallenge toggleVisibility() {
        return challenge(
                "toggle-visibility",
                "Toggle Visibility",
                Difficulty.EASY,
                true,
                2,
                "Show or hide a message with a toggle button that reflects the current state.",
                """
                # Toggle Visibility

                Build a component that reveals or hides a message.

                ## Requirements
                - Render a button with `data-testid="toggle"`.
                - When hidden, the button text is **Show**.
                - When visible, the button text is **Hide**.
                - Render the message `Hello BugForge` in an element with `data-testid="message"` only while visible.
                - The message is hidden on first render.

                ## Example
                Click Show → the message appears and the button reads Hide.
                Click Hide → the message is removed and the button reads Show.

                ## Constraints
                - Use boolean state.
                - Do not leave the message in the DOM while hidden.
                """,
                """
                import React, { useState } from "react";

                export default function ToggleVisibility() {
                  const [visible, setVisible] = useState(false);

                  return (
                    <div>
                      <button data-testid="toggle" onClick={() => setVisible(!visible)}>
                        {visible ? "Hide" : "Show"}
                      </button>
                    </div>
                  );
                }
                """,
                """
                {"cases":[
                  {"id":"starts-hidden","name":"Message is hidden initially","steps":[{"type":"assertText","testId":"toggle","equals":"Show"},{"type":"assertNotExists","testId":"message"}]},
                  {"id":"shows-message","name":"Clicking Show reveals the message","steps":[{"type":"click","testId":"toggle"},{"type":"assertText","testId":"message","equals":"Hello BugForge"},{"type":"assertText","testId":"toggle","equals":"Hide"}]},
                  {"id":"hides-again","name":"Clicking Hide restores the original state","steps":[{"type":"click","testId":"toggle"},{"type":"click","testId":"toggle"},{"type":"assertNotExists","testId":"message"},{"type":"assertText","testId":"toggle","equals":"Show"}]}
                ]}
                """
        );
    }

    private static SeedChallenge characterCounter() {
        return challenge(
                "character-counter",
                "Character Counter",
                Difficulty.EASY,
                true,
                3,
                "Track the number of characters in a textarea as the user types.",
                """
                # Character Counter

                Display a live character count for a textarea.

                ## Requirements
                - Render a textarea with `data-testid="input"`.
                - Render the current length in `data-testid="count"`.
                - The count starts at 0.
                - Update the count on every change.
                - Enforce a maximum of 50 characters using the textarea `maxLength`.

                ## Example
                Typing `Hi` shows `2`.

                ## Constraints
                - Controlled or uncontrolled is allowed as long as the displayed count stays in sync.
                """,
                """
                import React, { useState } from "react";

                export default function CharacterCounter() {
                  const [value, setValue] = useState("");

                  return (
                    <div>
                      <textarea
                        data-testid="input"
                        maxLength={50}
                        value={value}
                        onChange={(event) => setValue(event.target.value)}
                      />
                      <p data-testid="count">{value.length}</p>
                    </div>
                  );
                }
                """,
                """
                {"cases":[
                  {"id":"starts-zero","name":"Count starts at 0","steps":[{"type":"assertText","testId":"count","equals":"0"}]},
                  {"id":"counts-text","name":"Typing updates the character count","steps":[{"type":"type","testId":"input","value":"Hello"},{"type":"assertText","testId":"count","equals":"5"}]},
                  {"id":"max-length","name":"Textarea enforces a max length of 50","steps":[{"type":"assertAttribute","testId":"input","name":"maxlength","equals":"50"}]}
                ]}
                """
        );
    }

    private static SeedChallenge controlledInput() {
        return challenge(
                "controlled-input",
                "Controlled Input",
                Difficulty.EASY,
                true,
                4,
                "Keep an input and a live preview in sync using controlled React state.",
                """
                # Controlled Input

                Mirror typed text in a preview panel.

                ## Requirements
                - Render an input with `data-testid="name-input"`.
                - Render the current value in `data-testid="preview"`.
                - When the input is empty, preview must show `Anonymous`.
                - The input starts empty.

                ## Example
                Typing `Ada` shows `Ada`. Clearing the field shows `Anonymous`.

                ## Constraints
                - The input must be controlled (`value` + `onChange`).
                """,
                """
                import React, { useState } from "react";

                export default function ControlledInput() {
                  const [name, setName] = useState("");

                  return (
                    <div>
                      <input
                        data-testid="name-input"
                        value={name}
                        onChange={(event) => setName(event.target.value)}
                      />
                      <p data-testid="preview">{name || "Anonymous"}</p>
                    </div>
                  );
                }
                """,
                """
                {"cases":[
                  {"id":"default-anonymous","name":"Empty input shows Anonymous","steps":[{"type":"assertText","testId":"preview","equals":"Anonymous"}]},
                  {"id":"mirrors-value","name":"Preview mirrors typed text","steps":[{"type":"type","testId":"name-input","value":"Ada"},{"type":"assertText","testId":"preview","equals":"Ada"},{"type":"assertValue","testId":"name-input","equals":"Ada"}]}
                ]}
                """
        );
    }

    private static SeedChallenge showHidePassword() {
        return challenge(
                "show-hide-password",
                "Show/Hide Password",
                Difficulty.EASY,
                true,
                5,
                "Toggle a password field between masked and visible text.",
                """
                # Show/Hide Password

                Implement a password field with a visibility toggle.

                ## Requirements
                - Render an input with `data-testid="password"`.
                - The input starts as `type="password"`.
                - Render a button with `data-testid="toggle"`.
                - When hidden, the button text is **Show**.
                - When visible, the input type is `text` and the button text is **Hide**.

                ## Example
                Click Show → password is readable. Click Hide → it is masked again.

                ## Constraints
                - Preserve the typed value across toggles.
                """,
                """
                import React, { useState } from "react";

                export default function ShowHidePassword() {
                  const [visible, setVisible] = useState(false);
                  const [value, setValue] = useState("");

                  return (
                    <div>
                      <input
                        data-testid="password"
                        type={visible ? "text" : "password"}
                        value={value}
                        onChange={(event) => setValue(event.target.value)}
                      />
                      <button data-testid="toggle" onClick={() => setVisible(!visible)}>
                        {visible ? "Hide" : "Show"}
                      </button>
                    </div>
                  );
                }
                """,
                """
                {"cases":[
                  {"id":"starts-password","name":"Input starts as a password field","steps":[{"type":"assertAttribute","testId":"password","name":"type","equals":"password"},{"type":"assertText","testId":"toggle","equals":"Show"}]},
                  {"id":"shows-text","name":"Clicking Show makes the value visible","steps":[{"type":"click","testId":"toggle"},{"type":"assertAttribute","testId":"password","name":"type","equals":"text"},{"type":"assertText","testId":"toggle","equals":"Hide"}]},
                  {"id":"hides-again","name":"Clicking Hide masks the value again","steps":[{"type":"click","testId":"toggle"},{"type":"click","testId":"toggle"},{"type":"assertAttribute","testId":"password","name":"type","equals":"password"}]}
                ]}
                """
        );
    }

    private static SeedChallenge tabs() {
        return challenge(
                "tabs",
                "Tabs Component",
                Difficulty.EASY,
                true,
                6,
                "Switch between tab panels and highlight the active tab.",
                """
                # Tabs Component

                Render three tabs: Overview, Specs, and Reviews.

                ## Requirements
                - Render tab buttons with `data-testid="tab-overview"`, `tab-specs`, and `tab-reviews`.
                - Overview is selected on first render.
                - The selected tab has `aria-selected="true"`; others are `false`.
                - Render the active panel in `data-testid="panel"`.
                - Panel text: `Overview content`, `Specs content`, `Reviews content`.

                ## Example
                Click Specs → panel shows `Specs content` and only that tab is selected.

                ## Constraints
                - Keep a single source of selected-tab state.
                """,
                """
                import React, { useState } from "react";

                const TABS = [
                  { id: "overview", label: "Overview", testId: "tab-overview", content: "Overview content" },
                  { id: "specs", label: "Specs", testId: "tab-specs", content: "Specs content" },
                  { id: "reviews", label: "Reviews", testId: "tab-reviews", content: "Reviews content" }
                ];

                export default function Tabs() {
                  const [active, setActive] = useState("overview");
                  const current = TABS.find((tab) => tab.id === active);

                  return (
                    <div>
                      {TABS.map((tab) => (
                        <button
                          key={tab.id}
                          data-testid={tab.testId}
                          aria-selected={tab.id === active}
                          onClick={() => setActive(tab.id)}
                        >
                          {tab.label}
                        </button>
                      ))}
                      <div data-testid="panel">{current.content}</div>
                    </div>
                  );
                }
                """,
                """
                {"cases":[
                  {"id":"default-overview","name":"Overview is selected initially","steps":[{"type":"assertAttribute","testId":"tab-overview","name":"aria-selected","equals":"true"},{"type":"assertText","testId":"panel","equals":"Overview content"}]},
                  {"id":"switch-specs","name":"Clicking Specs shows specs content","steps":[{"type":"click","testId":"tab-specs"},{"type":"assertText","testId":"panel","equals":"Specs content"},{"type":"assertAttribute","testId":"tab-specs","name":"aria-selected","equals":"true"},{"type":"assertAttribute","testId":"tab-overview","name":"aria-selected","equals":"false"}]},
                  {"id":"switch-reviews","name":"Clicking Reviews shows reviews content","steps":[{"type":"click","testId":"tab-reviews"},{"type":"assertText","testId":"panel","equals":"Reviews content"}]}
                ]}
                """
        );
    }

    private static SeedChallenge searchableList() {
        return challenge(
                "searchable-list",
                "Searchable List",
                Difficulty.MEDIUM,
                true,
                7,
                "Filter a list of fruit names as the user types a case-insensitive query.",
                """
                # Searchable List

                Filter this list: Apple, Banana, Cherry, Date, Elderberry, Fig.

                ## Requirements
                - Render an input with `data-testid="search"`.
                - Render matching items as `li` elements with `data-testid="item"`.
                - Filtering is case-insensitive and matches substrings.
                - An empty query shows all 6 items.
                - When nothing matches, render `data-testid="empty"` with `No results`.

                ## Example
                Typing `an` shows Banana (and any other match). Typing `zzz` shows No results.

                ## Constraints
                - Do not mutate the original list; derive the filtered view.
                """,
                """
                import React, { useState } from "react";

                const FRUITS = ["Apple", "Banana", "Cherry", "Date", "Elderberry", "Fig"];

                export default function SearchableList() {
                  const [query, setQuery] = useState("");
                  const matches = FRUITS.filter((fruit) =>
                    fruit.toLowerCase().includes(query.toLowerCase())
                  );

                  return (
                    <div>
                      <input
                        data-testid="search"
                        value={query}
                        onChange={(event) => setQuery(event.target.value)}
                      />
                      {matches.length === 0 ? (
                        <p data-testid="empty">No results</p>
                      ) : (
                        <ul>
                          {matches.map((fruit) => (
                            <li key={fruit} data-testid="item">{fruit}</li>
                          ))}
                        </ul>
                      )}
                    </div>
                  );
                }
                """,
                """
                {"cases":[
                  {"id":"shows-all","name":"Empty query shows all items","steps":[{"type":"assertCount","testId":"item","equals":6}]},
                  {"id":"filters","name":"Query filters the list case-insensitively","steps":[{"type":"type","testId":"search","value":"an"},{"type":"assertCount","testId":"item","equals":1},{"type":"assertText","testId":"item","equals":"Banana"}]},
                  {"id":"empty-state","name":"Unmatched query shows No results","steps":[{"type":"type","testId":"search","value":"zzz"},{"type":"assertText","testId":"empty","equals":"No results"},{"type":"assertNotExists","testId":"item"}]}
                ]}
                """
        );
    }

    private static SeedChallenge todoList() {
        return challenge(
                "todo-list",
                "Todo List",
                Difficulty.MEDIUM,
                true,
                8,
                "Add todo items from an input, ignore blanks, and clear the field after each add.",
                """
                # Todo List

                Build a small todo list.

                ## Requirements
                - Input: `data-testid="todo-input"`.
                - Add button labeled **Add** (`data-testid="add"`).
                - Each item is `data-testid="todo-item"`.
                - Ignore empty or whitespace-only submissions.
                - After a successful add, clear the input.
                - Render a delete button inside each item with `data-testid="delete"`.

                ## Example
                Type `Write tests`, click Add → the item appears and the input is empty.
                Clicking delete removes that item.

                ## Constraints
                - Keep todos in component state as an array.
                """,
                """
                import React, { useState } from "react";

                export default function TodoList() {
                  const [text, setText] = useState("");
                  const [todos, setTodos] = useState([]);

                  function add() {
                    const value = text.trim();
                    if (!value) return;
                    setTodos((current) => [...current, { id: Date.now(), text: value }]);
                    setText("");
                  }

                  return (
                    <div>
                      <input
                        data-testid="todo-input"
                        value={text}
                        onChange={(event) => setText(event.target.value)}
                      />
                      <button data-testid="add" onClick={add}>Add</button>
                      <ul>
                        {todos.map((todo) => (
                          <li key={todo.id} data-testid="todo-item">
                            {todo.text}
                            <button
                              data-testid="delete"
                              onClick={() => setTodos((current) => current.filter((item) => item.id !== todo.id))}
                            >
                              Delete
                            </button>
                          </li>
                        ))}
                      </ul>
                    </div>
                  );
                }
                """,
                """
                {"cases":[
                  {"id":"adds-item","name":"Adds a new item from the input","steps":[{"type":"type","testId":"todo-input","value":"Write tests"},{"type":"click","testId":"add"},{"type":"assertCount","testId":"todo-item","equals":1},{"type":"assertText","testId":"todo-item","contains":"Write tests"}]},
                  {"id":"clears-input","name":"Clears the input after a successful add","steps":[{"type":"type","testId":"todo-input","value":"Ship it"},{"type":"click","testId":"add"},{"type":"assertValue","testId":"todo-input","equals":""}]},
                  {"id":"rejects-empty","name":"Does not add empty items","steps":[{"type":"type","testId":"todo-input","value":"   "},{"type":"click","testId":"add"},{"type":"assertNotExists","testId":"todo-item"}]},
                  {"id":"deletes-item","name":"Delete removes the item","steps":[{"type":"type","testId":"todo-input","value":"Temp"},{"type":"click","testId":"add"},{"type":"click","testId":"delete"},{"type":"assertNotExists","testId":"todo-item"}]}
                ]}
                """
        );
    }

    private static SeedChallenge accordion() {
        return challenge(
                "accordion",
                "Accordion",
                Difficulty.MEDIUM,
                true,
                9,
                "Expand one accordion section at a time and collapse it when selected again.",
                """
                # Accordion

                Render three sections: HTML, CSS, and JavaScript.

                ## Requirements
                - Headers: `data-testid="header-html"`, `header-css`, `header-js`.
                - Panels: `data-testid="panel-html"`, `panel-css`, `panel-js` — only while expanded.
                - Only one section may be open.
                - Clicking an open header closes it.
                - All sections start collapsed.
                - Panel copy: `Markup language`, `Styling language`, `Programming language`.

                ## Constraints
                - Store the open section id (or null) in state.
                """,
                """
                import React, { useState } from "react";

                const SECTIONS = [
                  { id: "html", headerTestId: "header-html", panelTestId: "panel-html", title: "HTML", body: "Markup language" },
                  { id: "css", headerTestId: "header-css", panelTestId: "panel-css", title: "CSS", body: "Styling language" },
                  { id: "js", headerTestId: "header-js", panelTestId: "panel-js", title: "JavaScript", body: "Programming language" }
                ];

                export default function Accordion() {
                  const [open, setOpen] = useState(null);

                  return (
                    <div>
                      {SECTIONS.map((section) => (
                        <div key={section.id}>
                          <button
                            data-testid={section.headerTestId}
                            onClick={() => setOpen((current) => current === section.id ? null : section.id)}
                          >
                            {section.title}
                          </button>
                          {open === section.id && (
                            <p data-testid={section.panelTestId}>{section.body}</p>
                          )}
                        </div>
                      ))}
                    </div>
                  );
                }
                """,
                """
                {"cases":[
                  {"id":"starts-collapsed","name":"All panels start collapsed","steps":[{"type":"assertNotExists","testId":"panel-html"},{"type":"assertNotExists","testId":"panel-css"}]},
                  {"id":"opens-one","name":"Opening a section shows its panel","steps":[{"type":"click","testId":"header-html"},{"type":"assertText","testId":"panel-html","equals":"Markup language"}]},
                  {"id":"exclusive","name":"Opening another section closes the previous one","steps":[{"type":"click","testId":"header-html"},{"type":"click","testId":"header-css"},{"type":"assertNotExists","testId":"panel-html"},{"type":"assertText","testId":"panel-css","equals":"Styling language"}]},
                  {"id":"toggle-close","name":"Clicking the open header collapses it","steps":[{"type":"click","testId":"header-js"},{"type":"click","testId":"header-js"},{"type":"assertNotExists","testId":"panel-js"}]}
                ]}
                """
        );
    }

    private static SeedChallenge modalDialog() {
        return challenge(
                "modal-dialog",
                "Modal Dialog",
                Difficulty.MEDIUM,
                true,
                10,
                "Open a dialog, keep page content behind it, and close via Cancel, Confirm, or overlay.",
                """
                # Modal Dialog

                Implement an accessible-enough modal for this MVP.

                ## Requirements
                - Open button: `data-testid="open"` labeled **Open dialog**.
                - While open, render `data-testid="overlay"` and `data-testid="dialog"`.
                - Dialog title text: `Confirm delete`.
                - Buttons **Cancel** (`data-testid="cancel"`) and **Confirm** (`data-testid="confirm"`).
                - Closing via overlay, Cancel, or Confirm removes the dialog.
                - After Confirm, show `data-testid="status"` with `Deleted`.
                - The dialog is closed initially.

                ## Constraints
                - Do not render dialog nodes while closed.
                """,
                """
                import React, { useState } from "react";

                export default function ModalDialog() {
                  const [open, setOpen] = useState(false);
                  const [status, setStatus] = useState("");

                  return (
                    <div>
                      <button data-testid="open" onClick={() => setOpen(true)}>Open dialog</button>
                      {status && <p data-testid="status">{status}</p>}
                      {open && (
                        <div data-testid="overlay" onClick={() => setOpen(false)}>
                          <div data-testid="dialog" onClick={(event) => event.stopPropagation()}>
                            <h2>Confirm delete</h2>
                            <button data-testid="cancel" onClick={() => setOpen(false)}>Cancel</button>
                            <button
                              data-testid="confirm"
                              onClick={() => {
                                setStatus("Deleted");
                                setOpen(false);
                              }}
                            >
                              Confirm
                            </button>
                          </div>
                        </div>
                      )}
                    </div>
                  );
                }
                """,
                """
                {"cases":[
                  {"id":"closed-initially","name":"Dialog is closed initially","steps":[{"type":"assertNotExists","testId":"dialog"}]},
                  {"id":"opens","name":"Open dialog shows the modal","steps":[{"type":"click","testId":"open"},{"type":"assertExists","testId":"dialog"},{"type":"assertText","selector":"[data-testid=dialog]","contains":"Confirm delete"}]},
                  {"id":"cancel-closes","name":"Cancel closes the dialog","steps":[{"type":"click","testId":"open"},{"type":"click","testId":"cancel"},{"type":"assertNotExists","testId":"dialog"}]},
                  {"id":"confirm-status","name":"Confirm closes the dialog and shows Deleted","steps":[{"type":"click","testId":"open"},{"type":"click","testId":"confirm"},{"type":"assertNotExists","testId":"dialog"},{"type":"assertText","testId":"status","equals":"Deleted"}]},
                  {"id":"overlay-closes","name":"Clicking the overlay closes the dialog","steps":[{"type":"click","testId":"open"},{"type":"click","testId":"overlay"},{"type":"assertNotExists","testId":"dialog"}]}
                ]}
                """
        );
    }

    private static SeedChallenge formValidation() {
        return challenge(
                "form-validation",
                "Form Validation",
                Difficulty.MEDIUM,
                false,
                11,
                "Validate email and password fields and surface field-level error messages.",
                """
                # Form Validation

                Validate a signup form before accepting it.

                ## Requirements
                - Email input: `data-testid="email"`.
                - Password input: `data-testid="password"`.
                - Submit button: `data-testid="submit"`.
                - Email must contain `@`.
                - Password must be at least 8 characters.
                - Show errors in `data-testid="email-error"` and `data-testid="password-error"` after submit.
                - Error copy: `Enter a valid email` and `Password must be at least 8 characters`.
                - On success, render `data-testid="success"` with `Account created`.
                - Clear errors after a successful submit.

                ## Constraints
                - Validate on submit, not only on change.
                """,
                """
                import React, { useState } from "react";

                export default function FormValidation() {
                  const [email, setEmail] = useState("");
                  const [password, setPassword] = useState("");
                  const [errors, setErrors] = useState({});
                  const [success, setSuccess] = useState(false);

                  function onSubmit(event) {
                    event.preventDefault();
                    const next = {};
                    if (!email.includes("@")) next.email = "Enter a valid email";
                    if (password.length < 8) next.password = "Password must be at least 8 characters";
                    setErrors(next);
                    setSuccess(Object.keys(next).length === 0);
                  }

                  return (
                    <form onSubmit={onSubmit}>
                      <input data-testid="email" value={email} onChange={(e) => setEmail(e.target.value)} />
                      {errors.email && <p data-testid="email-error">{errors.email}</p>}
                      <input data-testid="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
                      {errors.password && <p data-testid="password-error">{errors.password}</p>}
                      <button data-testid="submit" type="submit">Create account</button>
                      {success && <p data-testid="success">Account created</p>}
                    </form>
                  );
                }
                """,
                """
                {"cases":[
                  {"id":"invalid-email","name":"Rejects an email without @","steps":[{"type":"type","testId":"email","value":"not-an-email"},{"type":"type","testId":"password","value":"password123"},{"type":"click","testId":"submit"},{"type":"assertText","testId":"email-error","equals":"Enter a valid email"},{"type":"assertNotExists","testId":"success"}]},
                  {"id":"short-password","name":"Rejects a short password","steps":[{"type":"type","testId":"email","value":"ada@example.com"},{"type":"type","testId":"password","value":"short"},{"type":"click","testId":"submit"},{"type":"assertText","testId":"password-error","equals":"Password must be at least 8 characters"}]},
                  {"id":"success","name":"Accepts a valid form","steps":[{"type":"type","testId":"email","value":"ada@example.com"},{"type":"type","testId":"password","value":"password123"},{"type":"click","testId":"submit"},{"type":"assertText","testId":"success","equals":"Account created"},{"type":"assertNotExists","testId":"email-error"}]}
                ]}
                """
        );
    }

    private static SeedChallenge starRating() {
        return challenge(
                "star-rating",
                "Star Rating",
                Difficulty.MEDIUM,
                false,
                12,
                "Let users choose a 1–5 star rating and persist the selected value.",
                """
                # Star Rating

                Render five star buttons.

                ## Requirements
                - Stars use `data-testid="star"` and `data-value` of 1 through 5.
                - Selected stars (value <= rating) have `data-selected="true"`.
                - Show the numeric rating in `data-testid="rating"` (`0` initially).
                - Clicking a star sets that rating.
                - Clicking the same star again is allowed to keep the rating (no toggle-off required).

                ## Example
                Click the 4th star → rating is 4 and the first four stars are selected.
                """,
                """
                import React, { useState } from "react";

                export default function StarRating() {
                  const [rating, setRating] = useState(0);

                  return (
                    <div>
                      {[1, 2, 3, 4, 5].map((value) => (
                        <button
                          key={value}
                          data-testid="star"
                          data-value={value}
                          data-selected={value <= rating}
                          onClick={() => setRating(value)}
                        >
                          {value <= rating ? "★" : "☆"}
                        </button>
                      ))}
                      <p data-testid="rating">{rating}</p>
                    </div>
                  );
                }
                """,
                """
                {"cases":[
                  {"id":"starts-zero","name":"Rating starts at 0","steps":[{"type":"assertText","testId":"rating","equals":"0"},{"type":"assertCount","testId":"star","equals":5}]},
                  {"id":"selects-four","name":"Clicking the fourth star sets rating to 4","steps":[{"type":"click","selector":"[data-testid=star][data-value='4']"},{"type":"assertText","testId":"rating","equals":"4"},{"type":"assertAttribute","selector":"[data-testid=star][data-value='4']","name":"data-selected","equals":"true"},{"type":"assertAttribute","selector":"[data-testid=star][data-value='5']","name":"data-selected","equals":"false"}]}
                ]}
                """
        );
    }

    private static SeedChallenge debouncedSearch() {
        return challenge(
                "debounced-search",
                "Debounced Search",
                Difficulty.MEDIUM,
                false,
                13,
                "Delay emitting a search query until the user pauses typing for 300ms.",
                """
                # Debounced Search

                Debounce an input by 300 milliseconds.

                ## Requirements
                - Input: `data-testid="query"`.
                - Immediate value is not required in the output.
                - Render the debounced value in `data-testid="debounced"`.
                - The debounced node starts as an empty string.
                - After the user stops typing for 300ms, copy the input into the debounced output.

                ## Example
                Type `react` quickly; `debounced` stays empty until 300ms after the last keystroke, then shows `react`.

                ## Constraints
                - Clear pending timers on unmount or when the value changes.
                """,
                """
                import React, { useEffect, useState } from "react";

                export default function DebouncedSearch() {
                  const [query, setQuery] = useState("");
                  const [debounced, setDebounced] = useState("");

                  useEffect(() => {
                    const timer = setTimeout(() => setDebounced(query), 300);
                    return () => clearTimeout(timer);
                  }, [query]);

                  return (
                    <div>
                      <input data-testid="query" value={query} onChange={(e) => setQuery(e.target.value)} />
                      <p data-testid="debounced">{debounced}</p>
                    </div>
                  );
                }
                """,
                """
                {"cases":[
                  {"id":"starts-empty","name":"Debounced value starts empty","steps":[{"type":"assertText","testId":"debounced","equals":""}]},
                  {"id":"waits","name":"Does not update immediately while typing","steps":[{"type":"type","testId":"query","value":"re"},{"type":"assertText","testId":"debounced","equals":""}]},
                  {"id":"updates-after-pause","name":"Updates 300ms after the last keystroke","steps":[{"type":"type","testId":"query","value":"react"},{"type":"wait","ms":350},{"type":"assertText","testId":"debounced","equals":"react"}]}
                ]}
                """
        );
    }

    private static SeedChallenge pagination() {
        return challenge(
                "pagination",
                "Pagination Component",
                Difficulty.MEDIUM,
                false,
                14,
                "Page through a fixed list, five items at a time, with disabled edge buttons.",
                """
                # Pagination Component

                Paginate numbers 1 through 12 with a page size of 5.

                ## Requirements
                - Visible items: `data-testid="item"`.
                - Current page label: `data-testid="page"` (1-based).
                - Previous: `data-testid="prev"`; Next: `data-testid="next"`.
                - Disable Previous on page 1 and Next on the last page.
                - Page 1 shows 1–5, page 2 shows 6–10, page 3 shows 11–12.

                ## Constraints
                - Derive the slice from page state; do not hard-code three separate lists.
                """,
                """
                import React, { useState } from "react";

                const ITEMS = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];
                const PAGE_SIZE = 5;

                export default function Pagination() {
                  const [page, setPage] = useState(1);
                  const totalPages = Math.ceil(ITEMS.length / PAGE_SIZE);
                  const start = (page - 1) * PAGE_SIZE;
                  const visible = ITEMS.slice(start, start + PAGE_SIZE);

                  return (
                    <div>
                      <p data-testid="page">{page}</p>
                      <button data-testid="prev" disabled={page === 1} onClick={() => setPage((p) => p - 1)}>Previous</button>
                      <button data-testid="next" disabled={page === totalPages} onClick={() => setPage((p) => p + 1)}>Next</button>
                      <ul>
                        {visible.map((item) => (
                          <li key={item} data-testid="item">{item}</li>
                        ))}
                      </ul>
                    </div>
                  );
                }
                """,
                """
                {"cases":[
                  {"id":"first-page","name":"Page 1 shows the first five items","steps":[{"type":"assertText","testId":"page","equals":"1"},{"type":"assertCount","testId":"item","equals":5},{"type":"assertText","selector":"[data-testid=item]","equals":"1"},{"type":"assertAttribute","testId":"prev","name":"disabled","equals":""}]},
                  {"id":"next-page","name":"Next moves to page 2","steps":[{"type":"click","testId":"next"},{"type":"assertText","testId":"page","equals":"2"},{"type":"assertText","selector":"[data-testid=item]","equals":"6"}]},
                  {"id":"last-page","name":"The last page shows remaining items and disables Next","steps":[{"type":"click","testId":"next"},{"type":"click","testId":"next"},{"type":"assertText","testId":"page","equals":"3"},{"type":"assertCount","testId":"item","equals":2},{"type":"assertAttribute","testId":"next","name":"disabled","equals":""}]}
                ]}
                """
        );
    }

    private static SeedChallenge sortableTable() {
        return challenge(
                "sortable-table",
                "Sortable Table",
                Difficulty.MEDIUM,
                false,
                15,
                "Sort a table of products by name or price in ascending order.",
                """
                # Sortable Table

                Render rows for:
                - Banana / 8
                - Apple / 12
                - Cherry / 5

                ## Requirements
                - Rows: `tr` with `data-testid="row"`.
                - Each row contains `data-testid="name"` and `data-testid="price"`.
                - Buttons **Sort by name** (`data-testid="sort-name"`) and **Sort by price** (`data-testid="sort-price"`).
                - Name sort is alphabetical A–Z.
                - Price sort is numeric ascending.
                - Initial order is the original list.

                ## Example
                Sort by name → Apple, Banana, Cherry.
                Sort by price → Cherry, Banana, Apple.
                """,
                """
                import React, { useState } from "react";

                const INITIAL = [
                  { name: "Banana", price: 8 },
                  { name: "Apple", price: 12 },
                  { name: "Cherry", price: 5 }
                ];

                export default function SortableTable() {
                  const [rows, setRows] = useState(INITIAL);

                  return (
                    <div>
                      <button data-testid="sort-name" onClick={() => setRows([...rows].sort((a, b) => a.name.localeCompare(b.name)))}>
                        Sort by name
                      </button>
                      <button data-testid="sort-price" onClick={() => setRows([...rows].sort((a, b) => a.price - b.price))}>
                        Sort by price
                      </button>
                      <table>
                        <tbody>
                          {rows.map((row) => (
                            <tr key={row.name} data-testid="row">
                              <td data-testid="name">{row.name}</td>
                              <td data-testid="price">{row.price}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  );
                }
                """,
                """
                {"cases":[
                  {"id":"initial-order","name":"Starts in the original order","steps":[{"type":"assertText","selector":"[data-testid=row] [data-testid=name]","equals":"Banana"}]},
                  {"id":"sort-name","name":"Sort by name is alphabetical","steps":[{"type":"click","testId":"sort-name"},{"type":"assertText","selector":"[data-testid=row] [data-testid=name]","equals":"Apple"}]},
                  {"id":"sort-price","name":"Sort by price is numeric ascending","steps":[{"type":"click","testId":"sort-price"},{"type":"assertText","selector":"[data-testid=row] [data-testid=name]","equals":"Cherry"}]}
                ]}
                """
        );
    }

    private static SeedChallenge shoppingCart() {
        return challenge(
                "shopping-cart",
                "Shopping Cart",
                Difficulty.MEDIUM,
                false,
                16,
                "Add catalog items to a cart and keep a running total.",
                """
                # Shopping Cart

                Catalog:
                - Widget (`data-product="widget"`) costs 10
                - Gadget (`data-product="gadget"`) costs 15

                ## Requirements
                - Add buttons: `data-testid="add"` with `data-product`.
                - Cart lines: `data-testid="line"` containing the product name.
                - Quantity for a line: `data-testid="qty"`.
                - Adding the same product again increases quantity.
                - Total: `data-testid="total"` as an integer string with no currency symbol.
                - Empty cart total is `0`.

                ## Example
                Add Widget twice and Gadget once → total `35`.
                """,
                """
                import React, { useState } from "react";

                const CATALOG = [
                  { id: "widget", name: "Widget", price: 10 },
                  { id: "gadget", name: "Gadget", price: 15 }
                ];

                export default function ShoppingCart() {
                  const [cart, setCart] = useState({});

                  function add(id) {
                    setCart((current) => ({ ...current, [id]: (current[id] || 0) + 1 }));
                  }

                  const lines = CATALOG.filter((item) => cart[item.id]);
                  const total = lines.reduce((sum, item) => sum + item.price * cart[item.id], 0);

                  return (
                    <div>
                      {CATALOG.map((item) => (
                        <button key={item.id} data-testid="add" data-product={item.id} onClick={() => add(item.id)}>
                          Add {item.name}
                        </button>
                      ))}
                      <p data-testid="total">{total}</p>
                      {lines.map((item) => (
                        <div key={item.id} data-testid="line">
                          {item.name}
                          <span data-testid="qty">{cart[item.id]}</span>
                        </div>
                      ))}
                    </div>
                  );
                }
                """,
                """
                {"cases":[
                  {"id":"empty-total","name":"Empty cart total is 0","steps":[{"type":"assertText","testId":"total","equals":"0"}]},
                  {"id":"adds-quantity","name":"Adding the same product increases quantity","steps":[{"type":"click","selector":"[data-testid=add][data-product=widget]"},{"type":"click","selector":"[data-testid=add][data-product=widget]"},{"type":"assertText","testId":"qty","equals":"2"}]},
                  {"id":"computes-total","name":"Total includes mixed products","steps":[{"type":"click","selector":"[data-testid=add][data-product=widget]"},{"type":"click","selector":"[data-testid=add][data-product=widget]"},{"type":"click","selector":"[data-testid=add][data-product=gadget]"},{"type":"assertText","testId":"total","equals":"35"}]}
                ]}
                """
        );
    }

    private static SeedChallenge multiStepForm() {
        return challenge(
                "multi-step-form",
                "Multi-Step Form",
                Difficulty.MEDIUM,
                false,
                17,
                "Collect a name, then an email, then show a review step before finishing.",
                """
                # Multi-Step Form

                Three steps: Name → Email → Review.

                ## Requirements
                - Step label: `data-testid="step"` with `1`, `2`, or `3`.
                - Step 1: input `data-testid="name"` and **Next**.
                - Step 2: input `data-testid="email"` plus **Back** and **Next**.
                - Disable Next while the current field is empty/whitespace.
                - Step 3 shows `data-testid="summary-name"` and `data-testid="summary-email"`.
                - **Submit** on step 3 shows `data-testid="done"` with `Submitted`.

                ## Constraints
                - Preserve values when moving Back.
                """,
                """
                import React, { useState } from "react";

                export default function MultiStepForm() {
                  const [step, setStep] = useState(1);
                  const [name, setName] = useState("");
                  const [email, setEmail] = useState("");
                  const [done, setDone] = useState(false);

                  return (
                    <div>
                      <p data-testid="step">{step}</p>
                      {step === 1 && (
                        <div>
                          <input data-testid="name" value={name} onChange={(e) => setName(e.target.value)} />
                          <button disabled={!name.trim()} onClick={() => setStep(2)}>Next</button>
                        </div>
                      )}
                      {step === 2 && (
                        <div>
                          <input data-testid="email" value={email} onChange={(e) => setEmail(e.target.value)} />
                          <button onClick={() => setStep(1)}>Back</button>
                          <button disabled={!email.trim()} onClick={() => setStep(3)}>Next</button>
                        </div>
                      )}
                      {step === 3 && (
                        <div>
                          <p data-testid="summary-name">{name}</p>
                          <p data-testid="summary-email">{email}</p>
                          <button onClick={() => setDone(true)}>Submit</button>
                        </div>
                      )}
                      {done && <p data-testid="done">Submitted</p>}
                    </div>
                  );
                }
                """,
                """
                {"cases":[
                  {"id":"starts-step-one","name":"Starts on step 1","steps":[{"type":"assertText","testId":"step","equals":"1"}]},
                  {"id":"blocks-empty","name":"Next is disabled while the name is empty","steps":[{"type":"assertAttribute","selector":"button","name":"disabled","equals":""}]},
                  {"id":"review-and-submit","name":"Completing both steps shows a summary and submit","steps":[{"type":"type","testId":"name","value":"Ada"},{"type":"click","text":"Next"},{"type":"assertText","testId":"step","equals":"2"},{"type":"type","testId":"email","value":"ada@example.com"},{"type":"click","text":"Next"},{"type":"assertText","testId":"summary-name","equals":"Ada"},{"type":"assertText","testId":"summary-email","equals":"ada@example.com"},{"type":"click","text":"Submit"},{"type":"assertText","testId":"done","equals":"Submitted"}]}
                ]}
                """
        );
    }

    private static SeedChallenge autocomplete() {
        return challenge(
                "autocomplete",
                "Autocomplete Component",
                Difficulty.HARD,
                false,
                18,
                "Suggest cities from a fixed list, support keyboard highlighting, and fill the input on select.",
                """
                # Autocomplete Component

                Cities: London, Lisbon, Lima, Los Angeles, Berlin, Boston.

                ## Requirements
                - Input: `data-testid="query"`.
                - Suggestions: `data-testid="option"` shown when the query has at least 1 character.
                - Match case-insensitively from the start of the name (`Li` → Lisbon, Lima; not Berlin).
                - Highlight with `data-active="true"`; ArrowDown / ArrowUp move the highlight.
                - Enter or click fills the input and hides options.
                - Escape hides options without changing the input.

                ## Constraints
                - Clamp highlight indexes to the visible option list.
                """,
                """
                import React, { useState } from "react";

                const CITIES = ["London", "Lisbon", "Lima", "Los Angeles", "Berlin", "Boston"];

                export default function Autocomplete() {
                  const [query, setQuery] = useState("");
                  const [open, setOpen] = useState(false);
                  const [active, setActive] = useState(0);
                  const options = CITIES.filter((city) =>
                    city.toLowerCase().startsWith(query.toLowerCase())
                  );

                  function select(city) {
                    setQuery(city);
                    setOpen(false);
                  }

                  function onKeyDown(event) {
                    if (event.key === "ArrowDown") {
                      setActive((current) => Math.min(current + 1, Math.max(options.length - 1, 0)));
                    } else if (event.key === "ArrowUp") {
                      setActive((current) => Math.max(current - 1, 0));
                    } else if (event.key === "Enter" && open && options[active]) {
                      event.preventDefault();
                      select(options[active]);
                    } else if (event.key === "Escape") {
                      setOpen(false);
                    }
                  }

                  return (
                    <div>
                      <input
                        data-testid="query"
                        value={query}
                        onChange={(e) => {
                          setQuery(e.target.value);
                          setOpen(true);
                          setActive(0);
                        }}
                        onKeyDown={onKeyDown}
                      />
                      {open && query && options.map((city, index) => (
                        <button
                          key={city}
                          data-testid="option"
                          data-active={index === active}
                          onClick={() => select(city)}
                        >
                          {city}
                        </button>
                      ))}
                    </div>
                  );
                }
                """,
                """
                {"cases":[
                  {"id":"prefix-filter","name":"Filters cities by prefix","steps":[{"type":"type","testId":"query","value":"Li"},{"type":"assertCount","testId":"option","equals":2}]},
                  {"id":"click-select","name":"Clicking an option fills the input","steps":[{"type":"type","testId":"query","value":"Bo"},{"type":"click","text":"Boston"},{"type":"assertValue","testId":"query","equals":"Boston"},{"type":"assertNotExists","testId":"option"}]},
                  {"id":"keyboard-select","name":"ArrowDown then Enter selects the next option","steps":[{"type":"type","testId":"query","value":"Li"},{"type":"key","testId":"query","key":"ArrowDown"},{"type":"key","testId":"query","key":"Enter"},{"type":"assertValue","testId":"query","equals":"Lima"}]}
                ]}
                """
        );
    }

    private static SeedChallenge nestedComments() {
        return challenge(
                "nested-comments",
                "Nested Comments",
                Difficulty.HARD,
                false,
                19,
                "Render nested replies and allow adding a reply under an existing comment.",
                """
                # Nested Comments

                Seed one root comment: `Welcome` with `data-id="c1"`.

                ## Requirements
                - Each comment node: `data-testid="comment"` and `data-id`.
                - Comment text: `data-testid="body"`.
                - Reply button: `data-testid="reply"` inside the comment.
                - After clicking Reply, show input `data-testid="reply-input"` and **Post** (`data-testid="post-reply"`).
                - Posted replies appear nested inside the parent comment.
                - Ignore empty replies.
                - New ids may be generated; tests look up by body text.

                ## Example
                Reply `Hello` under Welcome → a nested comment with body Hello is visible.
                """,
                """
                import React, { useState } from "react";

                function Node({ node, onReply }) {
                  const [open, setOpen] = useState(false);
                  const [text, setText] = useState("");

                  return (
                    <div data-testid="comment" data-id={node.id}>
                      <p data-testid="body">{node.body}</p>
                      <button data-testid="reply" onClick={() => setOpen(true)}>Reply</button>
                      {open && (
                        <div>
                          <input data-testid="reply-input" value={text} onChange={(e) => setText(e.target.value)} />
                          <button
                            data-testid="post-reply"
                            onClick={() => {
                              if (!text.trim()) return;
                              onReply(node.id, text.trim());
                              setText("");
                              setOpen(false);
                            }}
                          >
                            Post
                          </button>
                        </div>
                      )}
                      {node.children.map((child) => (
                        <Node key={child.id} node={child} onReply={onReply} />
                      ))}
                    </div>
                  );
                }

                export default function NestedComments() {
                  const [tree, setTree] = useState([{ id: "c1", body: "Welcome", children: [] }]);

                  function addReply(parentId, body) {
                    const insert = (nodes) => nodes.map((node) => {
                      if (node.id === parentId) {
                        return {
                          ...node,
                          children: [...node.children, { id: `${parentId}-${node.children.length + 1}`, body, children: [] }]
                        };
                      }
                      return { ...node, children: insert(node.children) };
                    });
                    setTree((current) => insert(current));
                  }

                  return (
                    <div>
                      {tree.map((node) => (
                        <Node key={node.id} node={node} onReply={addReply} />
                      ))}
                    </div>
                  );
                }
                """,
                """
                {"cases":[
                  {"id":"seeded-root","name":"Renders the seeded root comment","steps":[{"type":"assertText","testId":"body","equals":"Welcome"}]},
                  {"id":"nested-reply","name":"Posting a reply nests it under the parent","steps":[{"type":"click","testId":"reply"},{"type":"type","testId":"reply-input","value":"Hello"},{"type":"click","testId":"post-reply"},{"type":"assertCount","testId":"comment","equals":2},{"type":"assertText","selector":"[data-testid=comment] [data-testid=comment] [data-testid=body]","equals":"Hello"}]},
                  {"id":"ignores-empty","name":"Empty replies are ignored","steps":[{"type":"click","testId":"reply"},{"type":"type","testId":"reply-input","value":"  "},{"type":"click","testId":"post-reply"},{"type":"assertCount","testId":"comment","equals":1}]}
                ]}
                """
        );
    }

    private static SeedChallenge dragAndDrop() {
        return challenge(
                "drag-and-drop-list",
                "Drag-and-Drop List",
                Difficulty.HARD,
                false,
                20,
                "Reorder a list of items using HTML drag-and-drop events.",
                """
                # Drag-and-Drop List

                Initial items: Alpha, Beta, Gamma.

                ## Requirements
                - Items: `li` with `data-testid="item"` and `draggable="true"`.
                - Reorder by dragging one item onto another.
                - Dropping item A on item B inserts A at B's index.
                - Keep labels unique and visible.

                The harness fires `dragstart` on the source and `drop` on the target.

                ## Example
                Drag Alpha onto Gamma → Beta, Gamma, Alpha or Gamma with Alpha inserted at that index.
                Tests assert that after dragging the first item onto the last item, the last item text becomes Alpha.
                """,
                """
                import React, { useState } from "react";

                export default function DragAndDropList() {
                  const [items, setItems] = useState(["Alpha", "Beta", "Gamma"]);
                  const [dragging, setDragging] = useState(null);

                  function onDrop(target) {
                    if (dragging == null || dragging === target) return;
                    setItems((current) => {
                      const next = [...current];
                      const [moved] = next.splice(dragging, 1);
                      next.splice(target, 0, moved);
                      return next;
                    });
                    setDragging(null);
                  }

                  return (
                    <ul>
                      {items.map((item, index) => (
                        <li
                          key={item}
                          data-testid="item"
                          draggable="true"
                          onDragStart={() => setDragging(index)}
                          onDragOver={(event) => event.preventDefault()}
                          onDrop={() => onDrop(index)}
                        >
                          {item}
                        </li>
                      ))}
                    </ul>
                  );
                }
                """,
                """
                {"cases":[
                  {"id":"initial-order","name":"Renders Alpha, Beta, Gamma","steps":[{"type":"assertCount","testId":"item","equals":3},{"type":"assertText","selector":"[data-testid=item]","equals":"Alpha"}]},
                  {"id":"reorder","name":"Dragging the first item onto the last reorders the list","steps":[{"type":"drag","from":"[data-testid=item]","fromIndex":0,"to":"[data-testid=item]","toIndex":2},{"type":"assertText","selector":"[data-testid=item]","index":2,"equals":"Alpha"}]}
                ]}
                """
        );
    }
}
