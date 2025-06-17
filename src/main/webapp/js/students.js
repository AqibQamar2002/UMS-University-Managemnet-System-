// Fetch all students
function fetchStudents() {
    fetch('/university-management-1.0-SNAPSHOT/students')
        .then(res => res.json())
        .then(data => {
            const tbody = document.querySelector('#studentsTable tbody');
            tbody.innerHTML = '';
            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" class="empty-state">No students found.</td></tr>';
            } else {
                data.forEach(s => {
                    tbody.innerHTML += `<tr>
                        <td>${s.studentId}</td>
                        <td>${s.firstName}</td>
                        <td>${s.lastName}</td>
                        <td>${s.dateOfBirth}</td>
                        <td>${s.email}</td>
                        <td>
                            <button class="action-btn" onclick="editStudent(${s.studentId}, '${s.firstName}', '${s.lastName}', '${s.dateOfBirth}', '${s.email}')">Edit</button>
                            <button class="action-btn delete" onclick="deleteStudent(${s.studentId})">Delete</button>
                        </td>
                    </tr>`;
                });
            }
        });
}

// Add new student
document.getElementById('studentForm').onsubmit = function(e) {
    e.preventDefault();
    const form = e.target;
    fetch('/university-management-1.0-SNAPSHOT/students', {
        method: 'POST',
        body: new URLSearchParams(new FormData(form))
    }).then(() => {
        form.reset();
        fetchStudents();
    });
};

// Delete student
function deleteStudent(id) {
    if (confirm('Are you sure you want to delete this student?')) {
        fetch('/university-management-1.0-SNAPSHOT/students?id=' + id, { method: 'DELETE' })
            .then(() => fetchStudents());
    }
}

// Edit student modal logic
const modal = document.getElementById('editStudentModal');
const closeModal = document.getElementById('closeEditStudentModal');

closeModal.onclick = function() { 
    modal.style.display = 'none'; 
};

window.onclick = function(event) { 
    if (event.target == modal) modal.style.display = 'none'; 
};

function editStudent(id, firstName, lastName, dateOfBirth, email) {
    document.getElementById('editStudentId').value = id;
    document.getElementById('editStudentFirstName').value = firstName;
    document.getElementById('editStudentLastName').value = lastName;
    document.getElementById('editStudentDateOfBirth').value = dateOfBirth;
    document.getElementById('editStudentEmail').value = email;
    modal.style.display = 'block';
}

// Update student
document.getElementById('editStudentForm').onsubmit = function(e) {
    e.preventDefault();
    const form = e.target;
    const params = new URLSearchParams(new FormData(form));
    params.append('_method', 'PUT');
    fetch('/university-management-1.0-SNAPSHOT/students', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        modal.style.display = 'none';
        fetchStudents();
    })
    .catch(error => {
        console.error('Error updating student:', error);
        alert('Error updating student. Please try again.');
    });
};

// Logout function
function logout() {
    fetch('/university-management-1.0-SNAPSHOT/logout', {
        method: 'POST',
        credentials: 'include'
    })
    .then(() => {
        window.location.href = 'login.html';
    });
}
function checkAuth() {
    fetch('/university-management-1.0-SNAPSHOT/check-auth', {
        method: 'GET',
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        if (!data.authenticated || data.role !== 'ADMIN') {
            window.location.href = 'login.html';
        } else {
            document.getElementById('userName').textContent = data.name;
        }
    })
    .catch(() => {
        window.location.href = 'login.html';
    });
}

// Initial check for authentication
checkAuth();
// Initial load
fetchStudents(); 