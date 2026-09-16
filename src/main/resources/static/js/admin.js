async function loadUsers() {
    const userList = document.getElementById("userList");

    try {
        const response = await fetch("/api/admin/users");

        if (!response.ok) {
            throw new Error("Unable to load users");
        }

        const users = await response.json();

        if (users.length === 0) {
            userList.innerHTML = "<p>No users found.</p>";
            return;
        }

        let html = `
            <table border="1" cellpadding="8" cellspacing="0">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Mobile</th>
                        <th>Registration Date</th>
<th>Last Login</th>
<th>Status</th>
<th>Role</th>
<th>Action</th>                    </tr>
                </thead>
                <tbody>
        `;

        users.forEach(user => {

            html += `
                <tr>
                    <td>${user.id}</td>
                    <td>${user.name}</td>
                    <td>${user.email}</td>
                    <td>${user.mobile}</td>
                    <td>${formatDate(user.registrationDate)}</td>
                    <td>${formatDate(user.lastLogin)}</td>
                    <td>
                        ${user.active ? "Active" : "Inactive"}
                    </td>
                    <td>
    ${user.role}
</td>
<td>
    ${user.role === "ADMIN"
                    ? "Admin Account"
                    : `
                <button
                    onclick="changeUserStatus(
                        ${user.id},
                        ${user.active}
                    )">
                    ${user.active ? "Deactivate" : "Activate"}
                </button>
              `
                }
</td>      </tr>
            `;
        });

        html += `
                </tbody>
            </table>
        `;

        userList.innerHTML = html;

    } catch (error) {

        userList.innerHTML =
            "<p>Unable to load users.</p>";

        console.error(error);
    }
}


async function loadActionHistory() {

    const historyContainer =
        document.getElementById("actionHistory");

    try {

        const response =
            await fetch("/api/admin/action-history");

        if (!response.ok) {
            throw new Error(
                "Unable to load action history"
            );
        }

        const history =
            await response.json();

        if (history.length === 0) {

            historyContainer.innerHTML =
                "<p>No admin actions recorded.</p>";

            return;
        }

        let html = `
            <table border="1" cellpadding="8" cellspacing="0">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Admin</th>
                        <th>Target User</th>
                        <th>Action</th>
                        <th>Reason</th>
                        <th>Date & Time</th>
                    </tr>
                </thead>
                <tbody>
        `;

        history.forEach(record => {

            html += `
                <tr>
                    <td>${record.id}</td>
                    <td>
                        ${record.adminName}
                        (ID: ${record.adminUserId})
                    </td>
                    <td>
                        ${record.targetUserName}
                        (ID: ${record.targetUserId})
                    </td>
                    <td>${record.action}</td>
                    <td>${record.reason}</td>
                    <td>${formatDate(record.actionDateTime)}</td>
                </tr>
            `;
        });

        html += `
                </tbody>
            </table>
        `;

        historyContainer.innerHTML = html;

    } catch (error) {

        historyContainer.innerHTML =
            "<p>Unable to load action history.</p>";

        console.error(error);
    }
}


async function changeUserStatus(
    userId,
    currentStatus
) {

    const newStatus = !currentStatus;

    const action =
        newStatus ? "activate" : "deactivate";

    const reason = prompt(
        `Enter reason to ${action} this user:`
    );

    if (reason === null) {
        return;
    }

    if (reason.trim() === "") {

        alert("Reason is mandatory.");

        return;
    }

    try {

        const response = await fetch(
            `/api/admin/users/${userId}/status`,
            {
                method: "PUT",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    active: newStatus,
                    reason: reason.trim()
                })
            }
        );

        const result =
            await response.text();

        if (!response.ok) {

            alert(result);

            return;
        }

        alert(result);

        await loadUsers();
        await loadActionHistory();

    } catch (error) {

        console.error(error);

        alert(
            "Unable to update user status."
        );
    }
}


function formatDate(dateValue) {

    if (!dateValue) {
        return "Never";
    }

    return new Date(dateValue)
        .toLocaleString();
}


async function logout() {

    try {

        const response =
            await fetch("/api/users/logout", {
                method: "POST"
            });

        if (response.ok) {

            window.location.href =
                "/login.html";

        } else {

            alert("Logout failed.");
        }

    } catch (error) {

        console.error(error);

        alert("Unable to logout.");
    }
}


async function initializeAdminPage() {

    await loadUsers();

    await loadActionHistory();
}


initializeAdminPage();