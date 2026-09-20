const periodSelect = document.getElementById("period");
const customDateSection = document.getElementById("customDateSection");

let editingTransactionId = null;





// ==============================
// Period Selection
// ==============================

periodSelect.addEventListener("change", function () {

    if (periodSelect.value === "CUSTOM") {

        customDateSection.style.display = "block";

    } else {

        customDateSection.style.display = "none";

    }

});


// ==============================
// Button Events
// ==============================

document
    .getElementById("loadDashboard")
    .addEventListener("click", loadDashboard);

document.getElementById("seeAllTransactions").addEventListener("click", showAllTransactions);

document
    .getElementById("applyFilters")
    .addEventListener("click", function () {
        loadDashboard(false);
    });

document
    .getElementById("clearFilters")
    .addEventListener("click", clearFilters);

document
    .getElementById("addTransaction")
    .addEventListener("click", addTransaction);

document
    .getElementById("generateReport")
    .addEventListener("click", generateReport);

document
    .getElementById("updateTransaction")
    .addEventListener("click", updateTransaction);

document
    .getElementById("cancelEdit")
    .addEventListener("click", cancelEdit);


// ==============================
// Add Transaction
// ==============================

async function addTransaction() {

    const transactionDate =
        document.getElementById("transactionDate").value;

    const type =
        document.getElementById("transactionType").value;

    const amount =
        document.getElementById("transactionAmount").value;

    const reason =
        document.getElementById("transactionReason").value.trim();


    if (!transactionDate) {

        alert("Please select a transaction date.");

        return;
    }


    if (!type) {

        alert("Please select transaction type.");

        return;
    }


    if (!amount || Number(amount) <= 0) {

        alert("Please enter an amount greater than ₹0.");

        return;
    }


    if (!reason) {

        alert("Please enter a reason.");

        return;
    }


    if (reason.length > 100) {

        alert("Reason cannot exceed 100 characters.");

        return;
    }


    const requestBody = {

        transactionDate: transactionDate,

        type: type,

        amount: Number(amount),

        reason: reason,

        saveAnyway: false,

        confirmUpdate: false

    };


    try {

        const response = await fetch(
            `/api/transactions`,
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify(requestBody)
            }
        );


        const data = await response.json();


        if (response.status === 409) {

            const saveAnyway = confirm(
                data.warning ||
                "A similar transaction already exists. Do you want to save this transaction anyway?"
            );


            if (!saveAnyway) {

                return;
            }


            requestBody.saveAnyway = true;


            const retryResponse = await fetch(
                `/api/transactions`,
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify(requestBody)
                }
            );


            const retryData =
                await retryResponse.json();


            if (!retryResponse.ok) {

                throw new Error(
                    retryData.error ||
                    "Unable to add transaction"
                );

            }


            alert("Transaction added successfully.");

        }

        else if (!response.ok) {

            throw new Error(
                data.error ||
                "Unable to add transaction"
            );

        }

        else {

            alert("Transaction added successfully.");

        }


        document.getElementById(
            "transactionDate"
        ).value = "";

        document.getElementById(
            "transactionType"
        ).value = "";

        document.getElementById(
            "transactionAmount"
        ).value = "";

        document.getElementById(
            "transactionReason"
        ).value = "";


        await loadDashboard();


    } catch (error) {

        alert(error.message);

    }

}
function showAllTransactions() {
    loadDashboard(true);
}

// ==============================
// Load Dashboard
// ==============================

async function loadDashboard(showAll = false) {
    const period =
        document.getElementById("period").value;


    const requestBody = {

        period: period,

        searchReason:
            document
                .getElementById("searchReason")
                .value
                .trim(),

        type:
            document
                .getElementById("filterType")
                .value || null,

        sortOrder:
            document
                .getElementById("sortOrder")
                .value,
        showAll: showAll

    };


    if (period === "CUSTOM") {

        requestBody.startDate =
            document.getElementById("startDate").value;

        requestBody.endDate =
            document.getElementById("endDate").value;


        if (
            !requestBody.startDate ||
            !requestBody.endDate
        ) {

            alert(
                "Please select both start date and end date."
            );

            return;
        }


        if (
            requestBody.startDate >
            requestBody.endDate
        ) {

            alert(
                "Start date cannot be after end date."
            );

            return;
        }

    }


    try {

        /*
         * Dashboard still uses userId for now.
         * We will secure DashboardController separately.
         */

        const response = await fetch(
            `/api/dashboard`,
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify(requestBody)
            }
        );


        const data =
            await response.json();


        if (!response.ok) {

            throw new Error(
                data.error ||
                "Unable to load dashboard"
            );

        }


        document.getElementById(
            "currentBalance"
        ).textContent =
            formatAmount(data.currentBalance);


        document.getElementById(
            "totalCredit"
        ).textContent =
            formatAmount(data.totalCredit);


        document.getElementById(
            "totalDebit"
        ).textContent =
            formatAmount(data.totalDebit);


        document.getElementById(
            "transactionCount"
        ).textContent =
            data.recentTransactions.length;

        displayTransactions(
            showAll
                ? data.recentTransactions
                : data.recentTransactions.slice(0, 10)
        );
        const seeAllButton = document.getElementById("seeAllTransactions");
        const historySubtitle = document.getElementById("transactionHistorySubtitle");

        if (showAll) {
            seeAllButton.style.display = "none";
            historySubtitle.textContent = "Showing all your transactions";
        } else {
            seeAllButton.style.display = "block";
            historySubtitle.textContent = "Showing your latest 10 transactions";
        }

        displayMonthlySummary(
            data.monthlySummary
        );
        if (
    showAll ||
    document.getElementById("searchReason").value.trim() ||
    document.getElementById("filterType").value
) {
    document
        .getElementById("transactionHistorySubtitle")
        .scrollIntoView({
            behavior: "smooth",
            block: "center"
        });
}


    } catch (error) {

        alert(error.message);

    }

}


// ==============================
// Clear Filters
// ==============================

function clearFilters() {

    document.getElementById(
        "searchReason"
    ).value = "";


    document.getElementById(
        "filterType"
    ).value = "";


    document.getElementById(
        "sortOrder"
    ).value = "NEWEST";


    loadDashboard(false);
}


// ==============================
// Format Amount
// ==============================

function formatAmount(amount) {

    return `₹${Number(amount).toLocaleString("en-IN", {

        minimumFractionDigits: 2,

        maximumFractionDigits: 2

    })}`;

}


// ==============================
// Display Transactions
// ==============================

function displayTransactions(transactions) {

    const tableBody =
        document.getElementById(
            "transactionTableBody"
        );


    tableBody.innerHTML = "";


    if (
        !transactions ||
        transactions.length === 0
    ) {

        const row =
            document.createElement("tr");


        row.innerHTML = `
            <td colspan="6">
                No transactions found.
            </td>
        `;


        tableBody.appendChild(row);

        return;
    }


    transactions.forEach(transaction => {

        const row =
            document.createElement("tr");


        let typeClass = "";

        let sign = "";


        if (transaction.type === "CREDIT") {

            typeClass = "credit-type";

            sign = "+";

        } else {

            typeClass = "debit-type";

            sign = "−";

        }


        row.innerHTML = `

            <td>
                ${transaction.transactionDate}
            </td>

            <td>
                <span class="transaction-type-badge ${typeClass}">
                    ${transaction.type}
                </span>
            </td>

            <td class="transaction-amount ${typeClass}">
                <span class="amount-sign">${sign}</span>
                ${formatAmount(transaction.amount).replace("₹", "")}
            </td>

            <td>
                ${transaction.reason}
            </td>

            <td>
                ${formatAmount(transaction.balance)}
            </td>

            <td>

                <button
                    onclick="editTransaction(${transaction.id})">
                    Edit
                </button>

                <button
                    onclick="deleteTransaction(${transaction.id})">
                    Delete
                </button>

            </td>

        `;


        tableBody.appendChild(row);

    });

}


// ==============================
// Display Monthly Summary
// ==============================

function displayMonthlySummary(monthlySummary) {

    const tableBody =
        document.getElementById(
            "monthlySummaryTableBody"
        );


    tableBody.innerHTML = "";


    if (
        !monthlySummary ||
        monthlySummary.length === 0
    ) {

        const row =
            document.createElement("tr");


        row.innerHTML = `
            <td colspan="4">
                No monthly summary available.
            </td>
        `;


        tableBody.appendChild(row);

        return;
    }


    monthlySummary.forEach(summary => {

        const row =
            document.createElement("tr");


        const monthName =
            new Date(
                summary.year,
                summary.month - 1,
                1
            ).toLocaleString(
                "en-IN",
                {
                    month: "long",
                    year: "numeric"
                }
            );


        row.innerHTML = `

            <td>
                ${monthName}
            </td>

            <td>
                ${formatAmount(summary.totalCredit)}
            </td>

            <td>
                ${formatAmount(summary.totalDebit)}
            </td>

            <td>
                ${formatAmount(summary.closingBalance)}
            </td>

        `;


        tableBody.appendChild(row);

    });

}


// ==============================
// Edit Transaction
// ==============================

async function editTransaction(transactionId) {

    try {

        /*
         * userId removed.
         * Backend identifies logged-in user.
         */

        const response = await fetch(
            `/api/transactions`
        );


        const transactions =
            await response.json();


        if (!response.ok) {

            throw new Error(
                transactions.error ||
                "Unable to load transaction"
            );

        }


        const transaction =
            transactions.find(
                item => item.id === transactionId
            );


        if (!transaction) {

            alert("Transaction not found.");

            return;
        }


        editingTransactionId =
            transactionId;


        document.getElementById(
            "editTransactionDate"
        ).value =
            transaction.transactionDate;


        document.getElementById(
            "editTransactionType"
        ).value =
            transaction.type;


        document.getElementById(
            "editTransactionAmount"
        ).value =
            transaction.amount;


        document.getElementById(
            "editTransactionReason"
        ).value =
            transaction.reason;


        document.getElementById(
            "editTransactionSection"
        ).style.display =
            "block";


        document.getElementById(
            "editTransactionSection"
        ).scrollIntoView({
            behavior: "smooth"
        });


    } catch (error) {

        alert(error.message);

    }

}


// ==============================
// Update Transaction
// ==============================

async function updateTransaction() {

    if (!editingTransactionId) {

        alert(
            "No transaction selected for editing."
        );

        return;
    }


    const transactionDate =
        document.getElementById(
            "editTransactionDate"
        ).value;


    const type =
        document.getElementById(
            "editTransactionType"
        ).value;


    const amount =
        document.getElementById(
            "editTransactionAmount"
        ).value;


    const reason =
        document.getElementById(
            "editTransactionReason"
        ).value.trim();


    if (!transactionDate) {

        alert(
            "Please select a transaction date."
        );

        return;
    }


    if (!type) {

        alert(
            "Please select transaction type."
        );

        return;
    }


    if (!amount || Number(amount) <= 0) {

        alert(
            "Please enter an amount greater than ₹0."
        );

        return;
    }


    if (!reason) {

        alert(
            "Please enter a reason."
        );

        return;
    }


    if (reason.length > 100) {

        alert(
            "Reason cannot exceed 100 characters."
        );

        return;
    }


    const confirmed =
        confirm(
            "Are you sure you want to update this transaction?"
        );


    if (!confirmed) {

        return;
    }


    const requestBody = {

        transactionDate: transactionDate,

        type: type,

        amount: Number(amount),

        reason: reason,

        saveAnyway: false,

        confirmUpdate: true

    };


    try {

        const response = await fetch(
            `/api/transactions/${editingTransactionId}`,
            {
                method: "PUT",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify(requestBody)
            }
        );


        const data =
            await response.json();


        if (!response.ok) {

            throw new Error(
                data.error ||
                "Unable to update transaction"
            );

        }


        alert(
            "Transaction updated successfully."
        );


        cancelEdit();


        await loadDashboard();


    } catch (error) {

        alert(error.message);

    }

}


// ==============================
// Cancel Edit
// ==============================

function cancelEdit() {

    editingTransactionId = null;


    document.getElementById(
        "editTransactionSection"
    ).style.display =
        "none";


    document.getElementById(
        "editTransactionDate"
    ).value = "";


    document.getElementById(
        "editTransactionType"
    ).value = "";


    document.getElementById(
        "editTransactionAmount"
    ).value = "";


    document.getElementById(
        "editTransactionReason"
    ).value = "";

}


// ==============================
// Delete Transaction
// ==============================

async function deleteTransaction(transactionId) {

    const confirmed =
        confirm(
            "Are you sure you want to delete this transaction?"
        );


    if (!confirmed) {

        return;
    }


    try {

        const response = await fetch(
            `/api/transactions/${transactionId}?confirmDelete=true`,
            {
                method: "DELETE"
            }
        );


        if (!response.ok) {

            const data =
                await response.json();


            throw new Error(
                data.error ||
                "Unable to delete transaction"
            );

        }


        alert(
            "Transaction deleted successfully."
        );


        await loadDashboard();


    } catch (error) {

        alert(error.message);

    }

}


// ==============================
// Generate Report
// ==============================

function generateReport() {

    const period =
        document.getElementById("period").value;


    if (period === "CURRENT_MONTH") {

        const today = new Date();


        const year =
            today.getFullYear();


        const month =
            today.getMonth() + 1;


        window.location.href =
            `report.html?year=${year}&month=${month}`;


        return;
    }


    if (period === "PREVIOUS_MONTH") {

        const today = new Date();


        today.setMonth(
            today.getMonth() - 1
        );


        const year =
            today.getFullYear();


        const month =
            today.getMonth() + 1;


        window.location.href =
            `report.html?year=${year}&month=${month}`;


        return;
    }


    if (period === "CUSTOM") {

        const startDate =
            document.getElementById(
                "startDate"
            ).value;


        const endDate =
            document.getElementById(
                "endDate"
            ).value;


        if (!startDate || !endDate) {

            alert(
                "Please select both start date and end date."
            );

            return;
        }


        if (startDate > endDate) {

            alert(
                "Start date cannot be after end date."
            );

            return;
        }


        const start =
            new Date(startDate);


        const end =
            new Date(endDate);


        const startYear =
            start.getFullYear();


        const startMonth =
            start.getMonth() + 1;


        const endYear =
            end.getFullYear();


        const endMonth =
            end.getMonth() + 1;


        if (
            startYear !== endYear ||
            startMonth !== endMonth
        ) {

            alert(
                "For Generate Report, the custom date range must be within the same month and year."
            );

            return;
        }


        window.location.href =
            `report.html?year=${startYear}&month=${startMonth}`;

    }

}


// ==============================
// Automatically Load Dashboard
// ==============================

async function initializeDashboard() {

    await loadDashboard();

}

initializeDashboard();


// ==============================
// Sidebar Navigation
// ==============================

const sidebarLinks =
    document.querySelectorAll(
        ".sidebar-nav .nav-item"
    );


sidebarLinks.forEach(function (link) {

    link.addEventListener(
        "click",
        function (event) {

            const targetId =
                link.getAttribute("href");


            /*
             * Settings is not implemented yet.
             * Don't allow it to jump to the top.
             */

            if (
                !targetId ||
                targetId === "#"
            ) {

                event.preventDefault();

                return;
            }


            /*
             * Remove active state from
             * all sidebar links.
             */

            sidebarLinks.forEach(function (item) {

                item.classList.remove("active");

            });


            /*
             * Activate clicked navigation item.
             */

            link.classList.add("active");

        }
    );

});


// ==============================
// Mobile Sidebar
// ==============================

const mobileMenuButton =
    document.querySelector(
        ".mobile-menu-button"
    );


const mobileSidebar =
    document.querySelector(
        ".sidebar"
    );


const sidebarOverlay =
    document.querySelector(
        ".sidebar-overlay"
    );


function openMobileSidebar() {

    if (
        !mobileSidebar ||
        !sidebarOverlay
    ) {

        return;
    }


    mobileSidebar.classList.add(
        "mobile-open"
    );


    sidebarOverlay.classList.add(
        "active"
    );

}


function closeMobileSidebar() {

    if (
        !mobileSidebar ||
        !sidebarOverlay
    ) {

        return;
    }


    mobileSidebar.classList.remove(
        "mobile-open"
    );


    sidebarOverlay.classList.remove(
        "active"
    );

}


if (mobileMenuButton) {

    mobileMenuButton.addEventListener(
        "click",
        openMobileSidebar
    );

}


if (sidebarOverlay) {

    sidebarOverlay.addEventListener(
        "click",
        closeMobileSidebar
    );

}
// ==============================
// Load Current Logged-in User
// ==============================

async function loadCurrentUser() {

    try {

        const response = await fetch("/api/users/me");

        if (!response.ok) {
            throw new Error("Unable to load user details");
        }

        const user = await response.json();

        const userName = user.name || "User Account";
        const userInitial = userName.charAt(0).toUpperCase();

        const sidebarUserName =
            document.getElementById("sidebarUserName");

        const sidebarUserAvatar =
            document.getElementById("sidebarUserAvatar");

        const topbarUserAvatar =
            document.getElementById("topbarUserAvatar");

        if (sidebarUserName) {
            sidebarUserName.textContent = userName;
        }

        if (sidebarUserAvatar) {
            sidebarUserAvatar.textContent = userInitial;
        }

        if (topbarUserAvatar) {
            topbarUserAvatar.textContent = userInitial;
        }

    } catch (error) {

        console.error(
            "Unable to load user details:",
            error
        );

    }
}


// Load logged-in user's name and initial
loadCurrentUser();