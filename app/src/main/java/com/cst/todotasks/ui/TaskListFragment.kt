package com.cst.todotasks.ui

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cst.todotasks.R
import com.cst.todotasks.models.Todo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TaskListFragment : Fragment() {

    private lateinit var tasks: List<Todo>
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_task_list, container, false)
        setUpNavBar()
        setHasOptionsMenu(true)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.menu_clear -> {
                if (tasks.isEmpty()) false
                else {
                    deleteTasks()
                    true
                }
            }
            R.id.menu_filter -> {
                showFilteringPopUpMenu()
                true
            }
            else -> false
        }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tasks_fragment_menu, menu)
    }

    private fun deleteTasks() {
        tasks.forEach {
            if (it.isChecked) {
                GlobalScope.launch {
                    withContext(Dispatchers.IO) {
                        MainActivity.dao.deleteTodo(it)
                    }
                }
            }
        }

        tasks = tasks.filter {
            !it.isChecked
        }

        (recyclerView.adapter as TodoListAdapter).setNewLIst(tasks)
        (recyclerView.adapter as TodoListAdapter).notifyDataSetChanged()

        if (tasks.isEmpty()) emptyTasksHandler()
    }

    private fun setUpNavBar() {
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(false)
        actionBar?.title = "TODO"
    }


    private fun showFilteringPopUpMenu() {
        val view = activity?.findViewById<View>(R.id.menu_filter) ?: return
        PopupMenu(requireContext(), view).run {
            menuInflater.inflate(R.menu.filter_tasks, menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.active -> {
                        getData()
                        if (tasks.isNotEmpty()) {
                            (recyclerView.adapter as TodoListAdapter).setNewLIst(tasks.filter { todo ->
                                !todo.isChecked
                            })
                            (recyclerView.adapter as TodoListAdapter).notifyDataSetChanged()
                        }
                    }

                    R.id.completed -> {
                        getData()
                        if (tasks.isNotEmpty()) {
                            (recyclerView.adapter as TodoListAdapter).setNewLIst(tasks.filter { todo ->
                                todo.isChecked
                            })
                            (recyclerView.adapter as TodoListAdapter).notifyDataSetChanged()
                        }
                    }
                    else -> {
                        getData()
                        if (tasks.isNotEmpty()) {
                            (recyclerView.adapter as TodoListAdapter).setNewLIst(tasks)
                            (recyclerView.adapter as TodoListAdapter).notifyDataSetChanged()
                        }
                    }
                }
                true
            }
            show()
        }
    }

    @Synchronized
    private fun getData() {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                tasks = MainActivity.dao.getAll()
            }
        }
    }

    private fun setUp() {
        context?.let {

            val circleOptions = activity?.findViewById<Button>(R.id.circle_option_add)
            circleOptions?.setOnClickListener {
                addClickHandler()
            }

            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    tasks = MainActivity.dao.getAll()
                    withContext(Dispatchers.Main) {
                        if (tasks.isEmpty()) {
                            emptyTasksHandler()
                        } else {
                            showTodo(tasks)
                        }
                    }
                }
            }
        }
    }

    private fun emptyTasksHandler() {
        activity?.let {
            val allTasks = it.findViewById<TextView>(R.id.all_tasks_title)
            val emptyHolder = it.findViewById<LinearLayout>(R.id.empty_todo_list_container)

            allTasks.visibility = TextView.GONE
            emptyHolder.visibility = LinearLayout.VISIBLE


        }

    }

    private fun addClickHandler() {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, AddTodoFragment())
            .addToBackStack(AddTodoFragment::class.java.simpleName)
            .commit()
    }

    private fun showTodo(tasks: List<Todo>) {
        activity?.let {
            val allTasks = it.findViewById<TextView>(R.id.all_tasks_title)
            val emptyHolder = it.findViewById<LinearLayout>(R.id.empty_todo_list_container)
            recyclerView = it.findViewById(R.id.todo_recycler_view)

            allTasks.visibility = TextView.VISIBLE
            emptyHolder.visibility = LinearLayout.GONE
            recyclerView.visibility = RecyclerView.VISIBLE

            recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = TodoListAdapter(tasks)
        }

    }

    companion object {

        fun createInstance() = TaskListFragment()

    }

}